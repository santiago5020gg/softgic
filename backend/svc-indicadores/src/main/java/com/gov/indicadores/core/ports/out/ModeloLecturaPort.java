package com.gov.indicadores.core.ports.out;

import java.time.LocalDateTime;

/** Puerto de salida: escritura del modelo de lectura (estado actual + hechos + idempotencia). */
public interface ModeloLecturaPort {

    /** ¿Ya se procesó este evento? (guarda de idempotencia — escenario A5). */
    boolean yaProcesado(String eventId);

    /** Marca el evento como procesado (PK garantiza que no se cuente dos veces). */
    void marcarProcesado(String eventId);

    /** Alta de una solicitud en el modelo de estado actual. */
    void registrarSolicitud(String aggregateId, String estado, Long categoriaId, LocalDateTime registradoEn);

    /** Actualiza el estado vigente de una solicitud existente. */
    void actualizarEstado(String aggregateId, String estado);

    /** Registra un hecho de transición para la tendencia diaria. */
    void registrarHecho(LocalDateTime fecha, Long categoriaId, String estado);
}
