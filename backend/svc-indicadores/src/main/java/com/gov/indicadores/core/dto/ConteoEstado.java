package com.gov.indicadores.core.dto;

/** Cantidad de solicitudes en un estado dado. */
public record ConteoEstado(String estado, long total) {
}
