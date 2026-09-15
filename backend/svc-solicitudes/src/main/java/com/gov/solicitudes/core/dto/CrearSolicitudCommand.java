package com.gov.solicitudes.core.dto;

import com.gov.solicitudes.core.domain.Prioridad;

/** Comando para registrar una solicitud. El {@code solicitante} y {@code correlationId}
 *  provienen del contexto de la petición (por ahora, headers; con Keycloak, del JWT). */
public record CrearSolicitudCommand(
        String asunto,
        String descripcion,
        Long categoriaId,
        Prioridad prioridad,
        String solicitante,
        String correlationId) {
}
