package com.gov.solicitudes.core.exception;

/** Se lanza cuando se intenta un salto de estado no permitido (escenario A4). */
public class TransicionInvalidaException extends RuntimeException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
