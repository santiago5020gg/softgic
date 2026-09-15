package com.gov.solicitudes.infrastructure.adapter.in;

import com.gov.solicitudes.core.dto.AccionTransicion;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de la petición de transición. {@code observacion} aplica a RESOLVER; {@code motivo} a DEVOLVER. */
public record TransicionRequest(
        @NotNull AccionTransicion accion,
        String observacion,
        String motivo) {
}
