package com.gov.indicadores.core.dto;

import java.util.List;

/** Resumen agregado: solicitudes por estado y por categoría. */
public record ResumenDto(List<ConteoEstado> porEstado, List<ConteoCategoria> porCategoria) {
}
