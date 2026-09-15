package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, Long> {

    /** Lote de eventos pendientes de publicar, ordenados por inserción. */
    List<OutboxEntity> findTop50ByPublishedFalseOrderByIdAsc();
}
