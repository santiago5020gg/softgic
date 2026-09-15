package com.gov.indicadores.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.indicadores.core.dto.EventoEntrante;
import com.gov.indicadores.core.ports.in.ProcesadorEventos;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter de entrada: consume los eventos de solicitudes desde Kafka.
 *
 * <p>Frontera transaccional del caso de uso: la comprobación de idempotencia y las
 * escrituras del modelo de lectura ocurren en una sola transacción.
 */
@Component
public class SolicitudEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SolicitudEventConsumer.class);

    private final ProcesadorEventos procesador;
    private final ObjectMapper mapper;

    public SolicitudEventConsumer(ProcesadorEventos procesador, ObjectMapper mapper) {
        this.procesador = procesador;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "solicitudes.eventos", groupId = "svc-indicadores")
    @Transactional
    public void onEvento(String mensaje) {
        try {
            JsonNode n = mapper.readTree(mensaje);
            JsonNode payload = n.get("payload");
            String estado = texto(payload, "estado");
            Long categoriaId = (payload != null && payload.hasNonNull("categoriaId"))
                    ? payload.get("categoriaId").asLong() : null;
            EventoEntrante e = new EventoEntrante(
                    texto(n, "eventId"),
                    texto(n, "type"),
                    texto(n, "aggregateId"),
                    estado,
                    categoriaId,
                    parseFecha(texto(n, "occurredAt")));
            procesador.procesar(e);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("No se pudo procesar el evento: {}", mensaje, ex);
            throw new IllegalStateException("Evento no procesable", ex);
        }
    }

    private static String texto(JsonNode node, String campo) {
        if (node == null) {
            return null;
        }
        JsonNode v = node.get(campo);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    /** Acepta ISO LocalDateTime o Instant; si no se puede parsear, usa el momento actual. */
    private static LocalDateTime parseFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(valor), ZoneOffset.UTC);
            } catch (Exception ignored2) {
                return LocalDateTime.now();
            }
        }
    }
}
