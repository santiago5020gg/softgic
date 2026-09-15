package com.gov.solicitudes.core.exception;

/** Se lanza cuando no existe una solicitud con el identificador pedido. */
public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException(Long id) {
        super("No existe la solicitud con id " + id);
    }
}
