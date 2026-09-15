package com.gov.solicitudes.infrastructure.adapter.in;

import com.gov.solicitudes.core.domain.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de la petición para registrar una solicitud. */
public record CrearSolicitudRequest(
        @NotBlank @Size(max = 200) String asunto,
        @NotBlank String descripcion,
        @NotNull Long categoriaId,
        @NotNull Prioridad prioridad) {
}
