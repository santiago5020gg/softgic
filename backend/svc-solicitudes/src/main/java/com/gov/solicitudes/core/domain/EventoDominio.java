package com.gov.solicitudes.core.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de dominio publicado por el aggregate {@link Solicitud}.
 *
 * <p>El sobre sigue el contrato del reto: {@code eventId}, {@code occurredAt},
 * {@code aggregateId}, {@code type}, {@code version} y {@code correlationId}, más el
 * {@code payload} de negocio. En Fase 1 se persiste en la tabla {@code outbox} dentro de
 * la misma transacción; un relay lo publicará a Kafka en Fase 2.
 */
public record EventoDominio(
        String eventId,
        Instant occurredAt,
        String aggregateId,
        String type,
        int version,
        String correlationId,
        Map<String, Object> payload) {

    /** Crea un evento nuevo con id y marca temporal generados. */
    public static EventoDominio de(String type, String aggregateId, String correlationId,
                                   Map<String, Object> payload) {
        return new EventoDominio(
                UUID.randomUUID().toString(),
                Instant.now(),
                aggregateId,
                type,
                1,
                correlationId == null || correlationId.isBlank()
                        ? UUID.randomUUID().toString()
                        : correlationId,
                payload);
    }
}
