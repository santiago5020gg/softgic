package com.gov.solicitudes.core.exception;

/** Se lanza al registrar una solicitud con una categoría inexistente o inactiva. */
public class CategoriaInvalidaException extends RuntimeException {
    public CategoriaInvalidaException(Long categoriaId) {
        super("La categoría " + categoriaId + " no existe o no está activa");
    }
}
