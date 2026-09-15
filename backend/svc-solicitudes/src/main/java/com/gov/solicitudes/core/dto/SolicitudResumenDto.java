package com.gov.solicitudes.core.dto;

import java.time.LocalDateTime;

/** Vista resumida para la bandeja/listado (sin historial ni observaciones). */
public record SolicitudResumenDto(
        Long id,
        String codigo,
        String asunto,
        Long categoriaId,
        String prioridad,
        String estado,
        String solicitante,
        String analista,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn) {
}
