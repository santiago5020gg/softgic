package com.gov.solicitudes.core.usecase;

import com.gov.solicitudes.core.dto.CategoriaDto;
import com.gov.solicitudes.core.mapper.CategoriaMapper;
import com.gov.solicitudes.core.ports.in.CatalogoServicePort;
import com.gov.solicitudes.core.ports.out.CategoriaRepositoryPort;
import java.util.List;

/** Caso de uso de consulta del catálogo de categorías activas. */
public class CatalogoService implements CatalogoServicePort {

    private final CategoriaRepositoryPort categoriaRepository;
    private final CategoriaMapper mapper;

    public CatalogoService(CategoriaRepositoryPort categoriaRepository, CategoriaMapper mapper) {
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<CategoriaDto> listarActivas() {
        return categoriaRepository.findActivas().stream().map(mapper::toDto).toList();
    }
}
