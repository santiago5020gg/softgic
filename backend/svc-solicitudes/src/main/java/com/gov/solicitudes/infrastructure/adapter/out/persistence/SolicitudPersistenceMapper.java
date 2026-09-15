package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.gov.solicitudes.core.domain.HistorialEstado;
import com.gov.solicitudes.core.domain.Observacion;
import com.gov.solicitudes.core.domain.Solicitud;
import java.util.List;
import org.springframework.stereotype.Component;

/** Traduce entre el aggregate de dominio y las entidades JPA. */
@Component
public class SolicitudPersistenceMapper {

    /** Crea una entidad nueva a partir del aggregate (para inserción). */
    public SolicitudEntity toNewEntity(Solicitud s) {
        SolicitudEntity e = new SolicitudEntity();
        e.setCodigo(s.getCodigo());
        e.setAsunto(s.getAsunto());
        e.setDescripcion(s.getDescripcion());
        e.setCategoriaId(s.getCategoriaId());
        e.setPrioridad(s.getPrioridad());
        e.setEstado(s.getEstado());
        e.setSolicitante(s.getSolicitante());
        e.setAnalista(s.getAnalista());
        e.setCreadoEn(s.getCreadoEn());
        e.setActualizadoEn(s.getActualizadoEn());
        for (HistorialEstado h : s.getHistorial()) {
            e.addHistorial(toHistorialEntity(h));
        }
        for (Observacion o : s.getObservaciones()) {
            e.addObservacion(toObservacionEntity(o));
        }
        return e;
    }

    public HistorialEstadoEntity toHistorialEntity(HistorialEstado h) {
        return new HistorialEstadoEntity(
                h.getEstadoAnterior(), h.getEstadoNuevo(), h.getActor(), h.getMotivo(), h.getOcurridoEn());
    }

    public ObservacionEntity toObservacionEntity(Observacion o) {
        return new ObservacionEntity(o.getTexto(), o.getActor(), o.getCreadoEn());
    }

    /** Reconstruye el aggregate completo (con historial y observaciones). */
    public Solicitud toDomainCompleto(SolicitudEntity e) {
        List<Observacion> observaciones = e.getObservaciones().stream()
                .map(o -> Observacion.rehidratar(o.getId(), o.getTexto(), o.getActor(), o.getCreadoEn()))
                .toList();
        List<HistorialEstado> historial = e.getHistorial().stream()
                .map(h -> HistorialEstado.rehidratar(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(),
                        h.getActor(), h.getMotivo(), h.getOcurridoEn()))
                .toList();
        return Solicitud.rehidratar(
                e.getId(), e.getCodigo(), e.getAsunto(), e.getDescripcion(), e.getCategoriaId(),
                e.getPrioridad(), e.getEstado(), e.getSolicitante(), e.getAnalista(),
                e.getVersion() == null ? 0 : e.getVersion(), e.getCreadoEn(), e.getActualizadoEn(),
                observaciones, historial);
    }

    /** Reconstruye una vista resumida (sin tocar las colecciones perezosas). */
    public Solicitud toDomainResumen(SolicitudEntity e) {
        return Solicitud.rehidratar(
                e.getId(), e.getCodigo(), e.getAsunto(), e.getDescripcion(), e.getCategoriaId(),
                e.getPrioridad(), e.getEstado(), e.getSolicitante(), e.getAnalista(),
                e.getVersion() == null ? 0 : e.getVersion(), e.getCreadoEn(), e.getActualizadoEn(),
                null, null);
    }
}
