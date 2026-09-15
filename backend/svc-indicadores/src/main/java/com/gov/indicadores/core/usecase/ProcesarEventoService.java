package com.gov.indicadores.core.usecase;

import com.gov.indicadores.core.dto.EventoEntrante;
import com.gov.indicadores.core.ports.in.ProcesadorEventos;
import com.gov.indicadores.core.ports.out.ModeloLecturaPort;

/**
 * Caso de uso de ingesta de eventos (framework-free).
 *
 * <p>Idempotencia (escenario A5): si el evento ya fue procesado, se ignora. La frontera
 * transaccional la fija el adapter de entrada (el consumidor Kafka), de modo que la
 * comprobación, las escrituras del modelo de lectura y el marcado de procesado son atómicos.
 */
public class ProcesarEventoService implements ProcesadorEventos {

    private final ModeloLecturaPort modelo;

    public ProcesarEventoService(ModeloLecturaPort modelo) {
        this.modelo = modelo;
    }

    @Override
    public void procesar(EventoEntrante e) {
        if (e.eventId() != null && modelo.yaProcesado(e.eventId())) {
            return; // ya contado: no se duplica
        }
        if ("SolicitudRegistrada".equals(e.type())) {
            modelo.registrarSolicitud(e.aggregateId(), e.estado(), e.categoriaId(), e.occurredAt());
        } else {
            modelo.actualizarEstado(e.aggregateId(), e.estado());
        }
        modelo.registrarHecho(e.occurredAt(), e.categoriaId(), e.estado());
        modelo.marcarProcesado(e.eventId());
    }
}
