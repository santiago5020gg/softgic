package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.solicitudes.core.domain.EventoDominio;
import com.gov.solicitudes.core.ports.out.EventPublisherPort;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * Implementación del publicador de eventos mediante la tabla {@code outbox}: inserta el
 * evento en la misma transacción de negocio. El relay que lo publica a Kafka llega en Fase 2.
 */
@Component
public class OutboxPersistenceAdapter implements EventPublisherPort {

    private final OutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxPersistenceAdapter(OutboxJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(EventoDominio evento) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(evento.payload());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el payload del evento " + evento.type(), ex);
        }
        LocalDateTime occurredAt = LocalDateTime.ofInstant(evento.occurredAt(), ZoneOffset.UTC);
        repository.save(new OutboxEntity(
                evento.eventId(),
                evento.aggregateId(),
                evento.type(),
                evento.version(),
                evento.correlationId(),
                payloadJson,
                occurredAt));
    }
}
