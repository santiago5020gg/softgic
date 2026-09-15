package com.gov.solicitudes.core.domain;

import java.util.Map;
import java.util.Set;

/**
 * Estados de una solicitud y las transiciones permitidas entre ellos.
 *
 * <p>La máquina de estados vive en el dominio (no en el controlador ni en la base de
 * datos): {@link #puedeTransicionarA(Estado)} es la única fuente de verdad sobre qué
 * saltos son válidos. Esto resuelve el escenario de aceptación A4 (una transición
 * inválida como RESUELTA → REGISTRADA se rechaza como regla de negocio).
 */
public enum Estado {
    REGISTRADA,
    EN_ATENCION,
    RESUELTA,
    CERRADA;

    private static final Map<Estado, Set<Estado>> TRANSICIONES = Map.of(
            REGISTRADA, Set.of(EN_ATENCION),
            EN_ATENCION, Set.of(RESUELTA),
            RESUELTA, Set.of(EN_ATENCION, CERRADA),
            CERRADA, Set.of()
    );

    /** Indica si desde este estado se puede pasar al {@code destino}. */
    public boolean puedeTransicionarA(Estado destino) {
        return TRANSICIONES.getOrDefault(this, Set.of()).contains(destino);
    }
}
