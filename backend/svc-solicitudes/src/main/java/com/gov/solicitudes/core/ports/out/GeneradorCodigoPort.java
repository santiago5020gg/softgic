package com.gov.solicitudes.core.ports.out;

/** Puerto de salida: genera el identificador legible de una solicitud (SOL-2026-000001). */
public interface GeneradorCodigoPort {

    String siguienteCodigo();
}
