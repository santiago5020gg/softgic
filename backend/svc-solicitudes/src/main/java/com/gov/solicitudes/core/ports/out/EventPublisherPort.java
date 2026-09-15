package com.gov.solicitudes.core.ports.out;

import com.gov.solicitudes.core.domain.EventoDominio;
import java.util.List;

/**
 * Puerto de salida: publicación de eventos de dominio. En Fase 1 el adapter escribe en la
 * tabla {@code outbox} dentro de la transacción de negocio (nunca publica al broker antes
 * del commit); el relay a Kafka llega en Fase 2.
 */
public interface EventPublisherPort {

    void publicar(EventoDominio evento);

    default void publicar(List<EventoDominio> eventos) {
        eventos.forEach(this::publicar);
    }
}
