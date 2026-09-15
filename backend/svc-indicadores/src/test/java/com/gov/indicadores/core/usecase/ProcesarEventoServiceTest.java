package com.gov.indicadores.core.usecase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gov.indicadores.core.dto.EventoEntrante;
import com.gov.indicadores.core.ports.out.ModeloLecturaPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba del caso de uso de ingesta — sin Spring. Cubre la idempotencia (A5). */
@ExtendWith(MockitoExtension.class)
class ProcesarEventoServiceTest {

    @Mock
    ModeloLecturaPort modelo;

    @Test
    @DisplayName("A5: un evento ya procesado no se vuelve a contar")
    void eventoDuplicadoSeIgnora() {
        ProcesarEventoService service = new ProcesarEventoService(modelo);
        when(modelo.yaProcesado("evt-1")).thenReturn(true);

        service.procesar(new EventoEntrante("evt-1", "SolicitudTomada", "SOL-2026-000001",
                "EN_ATENCION", null, LocalDateTime.now()));

        verify(modelo, never()).actualizarEstado(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(modelo, never()).registrarSolicitud(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(modelo, never()).registrarHecho(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(modelo, never()).marcarProcesado(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("SolicitudRegistrada da de alta el estado actual, registra el hecho y marca procesado")
    void registradaSeProcesa() {
        ProcesarEventoService service = new ProcesarEventoService(modelo);
        LocalDateTime t = LocalDateTime.of(2026, 9, 15, 10, 0);
        when(modelo.yaProcesado("evt-2")).thenReturn(false);

        service.procesar(new EventoEntrante("evt-2", "SolicitudRegistrada", "SOL-2026-000002",
                "REGISTRADA", 5L, t));

        verify(modelo).registrarSolicitud("SOL-2026-000002", "REGISTRADA", 5L, t);
        verify(modelo).registrarHecho(t, 5L, "REGISTRADA");
        verify(modelo).marcarProcesado("evt-2");
    }

    @Test
    @DisplayName("Una transición actualiza el estado actual y marca procesado")
    void transicionActualizaEstado() {
        ProcesarEventoService service = new ProcesarEventoService(modelo);
        when(modelo.yaProcesado("evt-3")).thenReturn(false);

        service.procesar(new EventoEntrante("evt-3", "SolicitudResuelta", "SOL-2026-000003",
                "RESUELTA", null, LocalDateTime.now()));

        verify(modelo).actualizarEstado("SOL-2026-000003", "RESUELTA");
        verify(modelo).marcarProcesado("evt-3");
    }
}
