package com.gov.solicitudes.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gov.solicitudes.core.domain.Prioridad;
import com.gov.solicitudes.core.domain.Solicitud;
import com.gov.solicitudes.core.dto.CrearSolicitudCommand;
import com.gov.solicitudes.core.dto.SolicitudDetalleDto;
import com.gov.solicitudes.core.exception.CategoriaInvalidaException;
import com.gov.solicitudes.core.exception.TransicionInvalidaException;
import com.gov.solicitudes.core.mapper.SolicitudMapper;
import com.gov.solicitudes.core.ports.out.CategoriaRepositoryPort;
import com.gov.solicitudes.core.ports.out.EventPublisherPort;
import com.gov.solicitudes.core.ports.out.GeneradorCodigoPort;
import com.gov.solicitudes.core.ports.out.SolicitudRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas del caso de uso agregado con dobles de los puertos (Mockito). */
@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepositoryPort solicitudRepository;
    @Mock
    private CategoriaRepositoryPort categoriaRepository;
    @Mock
    private EventPublisherPort eventPublisher;
    @Mock
    private GeneradorCodigoPort generadorCodigo;

    private SolicitudService service;

    @BeforeEach
    void setUp() {
        service = new SolicitudService(solicitudRepository, categoriaRepository,
                eventPublisher, generadorCodigo, new SolicitudMapper());
    }

    @Test
    @DisplayName("registrar valida categoría, persiste y publica el evento")
    void registrarPersisteYPublica() {
        when(categoriaRepository.existsActivaById(1L)).thenReturn(true);
        when(generadorCodigo.siguienteCodigo()).thenReturn("SOL-2026-000001");
        when(solicitudRepository.save(any())).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            return Solicitud.rehidratar(10L, s.getCodigo(), s.getAsunto(), s.getDescripcion(),
                    s.getCategoriaId(), s.getPrioridad(), s.getEstado(), s.getSolicitante(),
                    s.getAnalista(), 0, s.getCreadoEn(), s.getActualizadoEn(),
                    s.getObservaciones(), s.getHistorial());
        });

        SolicitudDetalleDto dto = service.registrar(new CrearSolicitudCommand(
                "Asunto", "Descripción", 1L, Prioridad.ALTA, "solicitante1", "corr"));

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.codigo()).isEqualTo("SOL-2026-000001");
        assertThat(dto.estado()).isEqualTo("REGISTRADA");
        verify(solicitudRepository).save(any());
        verify(eventPublisher).publicar(anyList());
    }

    @Test
    @DisplayName("registrar con categoría inactiva lanza CategoriaInvalidaException y no persiste ni publica")
    void categoriaInactiva() {
        when(categoriaRepository.existsActivaById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(new CrearSolicitudCommand(
                "Asunto", "Descripción", 99L, Prioridad.BAJA, "solicitante1", "corr")))
                .isInstanceOf(CategoriaInvalidaException.class);

        verify(solicitudRepository, never()).save(any());
        verify(eventPublisher, never()).publicar(anyList());
    }

    @Test
    @DisplayName("una transición inválida propaga la excepción y no persiste ni publica (A4)")
    void transicionInvalidaNoPersiste() {
        Solicitud existente = Solicitud.registrar("SOL-2026-000002", "a", "d",
                1L, Prioridad.BAJA, "solicitante1", "corr");
        existente.pullEventos();
        when(solicitudRepository.findById(5L)).thenReturn(Optional.of(existente));

        // resolver desde REGISTRADA es inválido
        assertThatThrownBy(() -> service.resolver(5L, "obs", "analista1", "corr"))
                .isInstanceOf(TransicionInvalidaException.class);

        verify(solicitudRepository, never()).save(any());
        verify(eventPublisher, never()).publicar(anyList());
    }
}
