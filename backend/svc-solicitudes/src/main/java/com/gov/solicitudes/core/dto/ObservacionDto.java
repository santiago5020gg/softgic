package com.gov.solicitudes.core.dto;

import java.time.LocalDateTime;

public record ObservacionDto(String texto, String actor, LocalDateTime creadoEn) {
}
