package com.gov.solicitudes.core.usecase;

import com.gov.solicitudes.core.domain.Solicitud;
import com.gov.solicitudes.core.dto.CrearSolicitudCommand;
import com.gov.solicitudes.core.dto.FiltroSolicitudes;
import com.gov.solicitudes.core.dto.PageResult;
import com.gov.solicitudes.core.dto.SolicitudDetalleDto;
import com.gov.solicitudes.core.dto.SolicitudResumenDto;
import com.gov.solicitudes.core.exception.CategoriaInvalidaException;
import com.gov.solicitudes.core.exception.SolicitudNoEncontradaException;
import com.gov.solicitudes.core.mapper.SolicitudMapper;
import com.gov.solicitudes.core.ports.in.SolicitudServicePort;
import com.gov.solicitudes.core.ports.out.CategoriaRepositoryPort;
import com.gov.solicitudes.core.ports.out.EventPublisherPort;
import com.gov.solicitudes.core.ports.out.GeneradorCodigoPort;
import com.gov.solicitudes.core.ports.out.SolicitudRepositoryPort;
import java.util.function.Consumer;

/**
 * Servicio agregado que implementa todos los casos de uso de solicitudes. Clase plana,
 * libre de framework; el cableo y la transacción los aporta la infraestructura.
 *
 * <p>En cada operación de escritura el evento se persiste (outbox) en la misma unidad de
 * trabajo que el cambio de estado, de modo que nunca se publica antes del commit.
 */
public class SolicitudService implements SolicitudServicePort {

    private final SolicitudRepositoryPort solicitudRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final EventPublisherPort eventPublisher;
    private final GeneradorCodigoPort generadorCodigo;
    private final SolicitudMapper mapper;

    public SolicitudService(SolicitudRepositoryPort solicitudRepository,
                            CategoriaRepositoryPort categoriaRepository,
                            EventPublisherPort eventPublisher,
                            GeneradorCodigoPort generadorCodigo,
                            SolicitudMapper mapper) {
        this.solicitudRepository = solicitudRepository;
        this.categoriaRepository = categoriaRepository;
        this.eventPublisher = eventPublisher;
        this.generadorCodigo = generadorCodigo;
        this.mapper = mapper;
    }

    @Override
    public SolicitudDetalleDto registrar(CrearSolicitudCommand command) {
        if (!categoriaRepository.existsActivaById(command.categoriaId())) {
            throw new CategoriaInvalidaException(command.categoriaId());
        }
        String codigo = generadorCodigo.siguienteCodigo();
        Solicitud solicitud = Solicitud.registrar(
                codigo,
                command.asunto(),
                command.descripcion(),
                command.categoriaId(),
                command.prioridad(),
                command.solicitante(),
                command.correlationId());
        Solicitud guardada = solicitudRepository.save(solicitud);
        eventPublisher.publicar(solicitud.pullEventos());
        return mapper.toDetalle(guardada);
    }

    @Override
    public PageResult<SolicitudResumenDto> listar(FiltroSolicitudes filtro, int page, int size) {
        PageResult<Solicitud> resultado = solicitudRepository.search(filtro, page, size);
        return new PageResult<>(
                resultado.content().stream().map(mapper::toResumen).toList(),
                resultado.page(),
                resultado.size(),
                resultado.totalElements(),
                resultado.totalPages());
    }

    @Override
    public SolicitudDetalleDto detalle(Long id) {
        return solicitudRepository.findById(id)
                .map(mapper::toDetalle)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }

    @Override
    public SolicitudDetalleDto tomar(Long id, String analista, String correlationId) {
        return aplicar(id, s -> s.tomar(analista, correlationId));
    }

    @Override
    public SolicitudDetalleDto resolver(Long id, String observacion, String actor, String correlationId) {
        return aplicar(id, s -> s.resolver(observacion, actor, correlationId));
    }

    @Override
    public SolicitudDetalleDto devolver(Long id, String motivo, String actor, String correlationId) {
        return aplicar(id, s -> s.devolver(motivo, actor, correlationId));
    }

    @Override
    public SolicitudDetalleDto cerrar(Long id, String actor, String correlationId) {
        return aplicar(id, s -> s.cerrar(actor, correlationId));
    }

    /** Carga, aplica la transición de dominio, persiste y publica los eventos resultantes. */
    private SolicitudDetalleDto aplicar(Long id, Consumer<Solicitud> transicion) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));
        transicion.accept(solicitud);
        Solicitud guardada = solicitudRepository.save(solicitud);
        eventPublisher.publicar(solicitud.pullEventos());
        return mapper.toDetalle(guardada);
    }
}
