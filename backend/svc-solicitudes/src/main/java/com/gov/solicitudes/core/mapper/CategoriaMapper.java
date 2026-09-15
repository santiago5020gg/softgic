package com.gov.solicitudes.core.mapper;

import com.gov.solicitudes.core.domain.Categoria;
import com.gov.solicitudes.core.dto.CategoriaDto;

public class CategoriaMapper {

    public CategoriaDto toDto(Categoria c) {
        return new CategoriaDto(c.id(), c.nombre(), c.activo());
    }
}
