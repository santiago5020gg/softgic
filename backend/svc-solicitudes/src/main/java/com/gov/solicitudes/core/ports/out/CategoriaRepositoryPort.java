package com.gov.solicitudes.core.ports.out;

import com.gov.solicitudes.core.domain.Categoria;
import java.util.List;

/** Puerto de salida: consulta del catálogo de categorías. */
public interface CategoriaRepositoryPort {

    List<Categoria> findActivas();

    boolean existsActivaById(Long id);
}
