package com.gov.indicadores.core.dto;

/** Punto de la tendencia diaria: total de transiciones registradas ese día. */
public record PuntoTendencia(String fecha, long total) {
}
