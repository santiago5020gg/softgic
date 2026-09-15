package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {

    List<CategoriaEntity> findByActivoTrueOrderByNombreAsc();

    boolean existsByIdAndActivoTrue(Long id);
}
