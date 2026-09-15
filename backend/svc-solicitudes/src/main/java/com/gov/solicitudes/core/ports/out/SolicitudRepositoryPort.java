package com.gov.solicitudes.core.ports.out;

import com.gov.solicitudes.core.domain.Solicitud;
import com.gov.solicitudes.core.dto.FiltroSolicitudes;
import com.gov.solicitudes.core.dto.PageResult;
import java.util.Optional;

/** Puerto de salida: persistencia del aggregate Solicitud. */
public interface SolicitudRepositoryPort {

    Solicitud save(Solicitud solicitud);

    Optional<Solicitud> findById(Long id);

    PageResult<Solicitud> search(FiltroSolicitudes filtro, int page, int size);
}
