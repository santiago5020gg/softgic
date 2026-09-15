package com.gov.solicitudes.core.dto;

import java.util.List;

/** Resultado paginado genérico e independiente de Spring. */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
