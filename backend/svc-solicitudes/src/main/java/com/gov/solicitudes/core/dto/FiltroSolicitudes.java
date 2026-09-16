package com.gov.solicitudes.core.dto;

import com.gov.solicitudes.core.domain.Estado;

/**
 * Filtro opcional para el listado. Cualquier campo null significa "sin filtrar por él".
 *
 * <p>{@code solicitante} lo fija el adapter de entrada para acotar a un usuario cuando el rol
 * es SOLICITANTE (regla del reto: "consulta sus solicitudes"); null = ve todas (ANALISTA/SUPERVISOR).
 */
public record FiltroSolicitudes(Estado estado, Long categoriaId, String solicitante) {
}
