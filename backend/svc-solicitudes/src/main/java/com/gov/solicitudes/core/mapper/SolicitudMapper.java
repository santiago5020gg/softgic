package com.gov.solicitudes.core.mapper;

import com.gov.solicitudes.core.domain.HistorialEstado;
import com.gov.solicitudes.core.domain.Observacion;
import com.gov.solicitudes.core.domain.Solicitud;
import com.gov.solicitudes.core.dto.HistorialDto;
import com.gov.solicitudes.core.dto.ObservacionDto;
import com.gov.solicitudes.core.dto.SolicitudDetalleDto;
import com.gov.solicitudes.core.dto.SolicitudResumenDto;
import java.util.List;

/** Convierte el aggregate de dominio a sus DTOs de salida. Sin dependencias de framework. */
public class SolicitudMapper {

    public SolicitudResumenDto toResumen(Solicitud s) {
        return new SolicitudResumenDto(
                s.getId(),
                s.getCodigo(),
                s.getAsunto(),
                s.getCategoriaId(),
                s.getPrioridad().name(),
                s.getEstado().name(),
                s.getSolicitante(),
                s.getAnalista(),
                s.getCreadoEn(),
                s.getActualizadoEn());
    }

    public SolicitudDetalleDto toDetalle(Solicitud s) {
        List<HistorialDto> historial = s.getHistorial().stream()
                .map(this::toHistorialDto)
                .toList();
        List<ObservacionDto> observaciones = s.getObservaciones().stream()
                .map(this::toObservacionDto)
                .toList();
        return new SolicitudDetalleDto(
                s.getId(),
                s.getCodigo(),
                s.getAsunto(),
                s.getDescripcion(),
                s.getCategoriaId(),
                s.getPrioridad().name(),
                s.getEstado().name(),
                s.getSolicitante(),
                s.getAnalista(),
                s.getVersion(),
                s.getCreadoEn(),
                s.getActualizadoEn(),
                historial,
                observaciones);
    }

    private HistorialDto toHistorialDto(HistorialEstado h) {
        return new HistorialDto(
                h.getEstadoAnterior() == null ? null : h.getEstadoAnterior().name(),
                h.getEstadoNuevo().name(),
                h.getActor(),
                h.getMotivo(),
                h.getOcurridoEn());
    }

    private ObservacionDto toObservacionDto(Observacion o) {
        return new ObservacionDto(o.getTexto(), o.getActor(), o.getCreadoEn());
    }
}
