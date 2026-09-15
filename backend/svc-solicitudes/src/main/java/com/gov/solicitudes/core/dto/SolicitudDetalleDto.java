package com.gov.solicitudes.core.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Vista de detalle con línea de tiempo (historial) y observaciones. */
public record SolicitudDetalleDto(
        Long id,
        String codigo,
        String asunto,
        String descripcion,
        Long categoriaId,
        String prioridad,
        String estado,
        String solicitante,
        String analista,
        long version,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,
        List<HistorialDto> historial,
        List<ObservacionDto> observaciones) {
}
