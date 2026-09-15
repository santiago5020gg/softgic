package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.gov.solicitudes.core.domain.HistorialEstado;
import com.gov.solicitudes.core.domain.Observacion;
import com.gov.solicitudes.core.domain.Solicitud;
import com.gov.solicitudes.core.dto.FiltroSolicitudes;
import com.gov.solicitudes.core.dto.PageResult;
import com.gov.solicitudes.core.exception.SolicitudNoEncontradaException;
import com.gov.solicitudes.core.ports.out.SolicitudRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/** Adapter de persistencia del aggregate Solicitud. */
@Repository
public class SolicitudPersistenceAdapter implements SolicitudRepositoryPort {

    private final SolicitudJpaRepository repository;
    private final SolicitudPersistenceMapper mapper;

    public SolicitudPersistenceAdapter(SolicitudJpaRepository repository,
                                       SolicitudPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Solicitud save(Solicitud solicitud) {
        if (solicitud.getId() == null) {
            SolicitudEntity guardada = repository.save(mapper.toNewEntity(solicitud));
            return mapper.toDomainCompleto(guardada);
        }
        // Actualización: recupera la entidad gestionada y aplica los cambios de estado y los
        // nuevos hijos (historial/observaciones son append-only). @Version protege A2.
        SolicitudEntity entidad = repository.findById(solicitud.getId())
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitud.getId()));
        entidad.setEstado(solicitud.getEstado());
        entidad.setAnalista(solicitud.getAnalista());
        entidad.setActualizadoEn(solicitud.getActualizadoEn());
        for (HistorialEstado h : solicitud.getHistorial()) {
            if (h.getId() == null) {
                entidad.addHistorial(mapper.toHistorialEntity(h));
            }
        }
        for (Observacion o : solicitud.getObservaciones()) {
            if (o.getId() == null) {
                entidad.addObservacion(mapper.toObservacionEntity(o));
            }
        }
        SolicitudEntity guardada = repository.save(entidad);
        return mapper.toDomainCompleto(guardada);
    }

    @Override
    public Optional<Solicitud> findById(Long id) {
        return repository.findById(id).map(mapper::toDomainCompleto);
    }

    @Override
    public PageResult<Solicitud> search(FiltroSolicitudes filtro, int page, int size) {
        Page<SolicitudEntity> pagina = repository.findAll(
                build(filtro),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creadoEn")));
        List<Solicitud> contenido = pagina.getContent().stream()
                .map(mapper::toDomainResumen)
                .toList();
        return new PageResult<>(contenido, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages());
    }

    private Specification<SolicitudEntity> build(FiltroSolicitudes filtro) {
        Specification<SolicitudEntity> spec = (root, query, cb) -> cb.conjunction();
        if (filtro != null && filtro.estado() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), filtro.estado()));
        }
        if (filtro != null && filtro.categoriaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoriaId"), filtro.categoriaId()));
        }
        return spec;
    }
}
