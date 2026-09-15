package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.gov.solicitudes.core.ports.out.GeneradorCodigoPort;
import java.time.Year;
import org.springframework.stereotype.Component;

/**
 * Genera el identificador legible con formato {@code SOL-<año>-<secuencia 6 dígitos>}.
 *
 * <p>Fase 1: la secuencia se deriva del conteo actual. Bajo alta concurrencia dos altas
 * simultáneas podrían chocar contra la restricción UNIQUE de {@code codigo}; en Fase 2 se
 * reemplazará por una secuencia de base de datos.
 */
@Component
public class GeneradorCodigoAdapter implements GeneradorCodigoPort {

    private final SolicitudJpaRepository repository;

    public GeneradorCodigoAdapter(SolicitudJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public String siguienteCodigo() {
        long secuencia = repository.count() + 1;
        return String.format("SOL-%d-%06d", Year.now().getValue(), secuencia);
    }
}
