package com.gov.solicitudes.core.dto;

import java.time.LocalDateTime;

public record HistorialDto(
        String estadoAnterior,
        String estadoNuevo,
        String actor,
        String motivo,
        LocalDateTime ocurridoEn) {
}
