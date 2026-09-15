package com.gov.solicitudes.core.dto;

import com.gov.solicitudes.core.domain.Estado;

/** Filtro opcional para el listado. Cualquier campo null significa "sin filtrar por él". */
public record FiltroSolicitudes(Estado estado, Long categoriaId) {
}
