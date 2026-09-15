package com.gov.solicitudes.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gov.solicitudes.infrastructure.adapter.out.persistence.OutboxEntity;
import com.gov.solicitudes.infrastructure.adapter.out.persistence.OutboxJpaRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Relay del patrón Outbox: publica a Kafka los eventos que ya fueron confirmados en la
 * transacción de negocio (nunca antes del commit). Corre periódicamente, lee el lote de
 * filas no publicadas, publica cada una y la marca como publicada. Si la publicación de
 * una fila falla, se deja pendiente y se reintenta en el siguiente ciclo (at-least-once);
 * el consumidor es idempotente para tolerar duplicados.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final String TOPIC = "solicitudes.eventos";

    private final OutboxJpaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxJpaRepository repository,
                       KafkaTemplate<String, String> kafkaTemplate,
                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    public void publicarPendientes() {
        List<OutboxEntity> pendientes = repository.findTop50ByPublishedFalseOrderByIdAsc();
        for (OutboxEntity fila : pendientes) {
            try {
                String value = construirEnvelope(fila);
                kafkaTemplate.send(TOPIC, fila.getAggregateId(), value).get(5, TimeUnit.SECONDS);
                fila.setPublished(true);
                fila.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
                repository.save(fila);
            } catch (Exception ex) {
                // No rompemos el lote: la fila queda pendiente y se reintenta en el próximo ciclo.
                log.warn("No se pudo publicar el evento outbox id={} (type={}); se reintentará: {}",
                        fila.getId(), fila.getType(), ex.getMessage());
            }
        }
    }

    /** Construye el sobre del evento; el payload almacenado (JSON) se anida como objeto real. */
    private String construirEnvelope(OutboxEntity fila) throws Exception {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("eventId", fila.getEventId());
        envelope.put("type", fila.getType());
        envelope.put("aggregateId", fila.getAggregateId());
        envelope.put("version", fila.getVersion());
        envelope.put("correlationId", fila.getCorrelationId());
        envelope.put("occurredAt", fila.getOccurredAt().atOffset(ZoneOffset.UTC).toString());
        envelope.set("payload", objectMapper.readTree(fila.getPayload()));
        return objectMapper.writeValueAsString(envelope);
    }
}
