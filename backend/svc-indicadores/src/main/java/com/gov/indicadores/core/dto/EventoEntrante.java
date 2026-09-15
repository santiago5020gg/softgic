package com.gov.indicadores.core.dto;

import java.time.LocalDateTime;

/**
 * Evento de dominio ya deserializado en la frontera (el consumidor Kafka lo arma a
 * partir del sobre JSON). Es el DTO que cruza hacia el caso de uso.
 */
public record EventoEntrante(
        String eventId,
        String type,
        String aggregateId,
        String estado,
        Long categoriaId,
        LocalDateTime occurredAt) {
}
