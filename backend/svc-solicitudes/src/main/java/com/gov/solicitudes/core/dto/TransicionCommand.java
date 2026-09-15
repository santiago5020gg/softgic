package com.gov.solicitudes.core.dto;

/** Comando de transición unificado. {@code observacion} aplica a RESOLVER; {@code motivo}
 *  a DEVOLVER. El {@code actor} y {@code correlationId} vienen del contexto de la petición. */
public record TransicionCommand(
        AccionTransicion accion,
        String observacion,
        String motivo,
        String actor,
        String correlationId) {
}
