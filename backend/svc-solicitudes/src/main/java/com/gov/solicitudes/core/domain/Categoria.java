package com.gov.solicitudes.core.domain;

/** Categoría del catálogo. Solo lectura para el reto. */
public record Categoria(Long id, String nombre, boolean activo) {
}
