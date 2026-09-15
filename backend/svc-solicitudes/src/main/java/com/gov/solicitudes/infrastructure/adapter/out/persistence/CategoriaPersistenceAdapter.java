package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.gov.solicitudes.core.domain.Categoria;
import com.gov.solicitudes.core.ports.out.CategoriaRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository repository;

    public CategoriaPersistenceAdapter(CategoriaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Categoria> findActivas() {
        return repository.findByActivoTrueOrderByNombreAsc().stream()
                .map(c -> new Categoria(c.getId(), c.getNombre(), c.isActivo()))
                .toList();
    }

    @Override
    public boolean existsActivaById(Long id) {
        return id != null && repository.existsByIdAndActivoTrue(id);
    }
}
