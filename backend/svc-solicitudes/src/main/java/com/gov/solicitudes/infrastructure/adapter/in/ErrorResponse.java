package com.gov.solicitudes.infrastructure.adapter.in;

import java.time.LocalDateTime;

/** Cuerpo de error uniforme para toda la API. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
}
