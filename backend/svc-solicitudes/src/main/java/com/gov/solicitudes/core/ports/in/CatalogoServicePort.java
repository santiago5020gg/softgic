package com.gov.solicitudes.core.ports.in;

import com.gov.solicitudes.core.dto.CategoriaDto;
import java.util.List;

/** Puerto de entrada: consulta del catálogo de categorías. */
public interface CatalogoServicePort {

    List<CategoriaDto> listarActivas();
}
