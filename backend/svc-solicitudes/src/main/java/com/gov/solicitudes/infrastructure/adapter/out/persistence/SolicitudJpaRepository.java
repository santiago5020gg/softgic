package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SolicitudJpaRepository
        extends JpaRepository<SolicitudEntity, Long>, JpaSpecificationExecutor<SolicitudEntity> {
}
