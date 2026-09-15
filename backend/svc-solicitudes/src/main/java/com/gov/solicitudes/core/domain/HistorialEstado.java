package com.gov.solicitudes.core.domain;

import java.time.LocalDateTime;

/**
 * Entrada de trazabilidad: cada transición conserva estado anterior/nuevo, actor, motivo
 * y fecha. Es append-only (nunca se modifica ni se borra).
 */
public class HistorialEstado {

    private Long id;
    private Estado estadoAnterior;
    private Estado estadoNuevo;
    private String actor;
    private String motivo;
    private LocalDateTime ocurridoEn;

    private HistorialEstado() {
    }

    /** Nueva entrada de historial (aún sin persistir). {@code estadoAnterior} es null en el registro inicial. */
    public static HistorialEstado nuevo(Estado estadoAnterior, Estado estadoNuevo, String actor,
                                        String motivo, LocalDateTime ocurridoEn) {
        HistorialEstado h = new HistorialEstado();
        h.estadoAnterior = estadoAnterior;
        h.estadoNuevo = estadoNuevo;
        h.actor = actor;
        h.motivo = motivo;
        h.ocurridoEn = ocurridoEn;
        return h;
    }

    /** Reconstruye una entrada existente desde persistencia. */
    public static HistorialEstado rehidratar(Long id, Estado estadoAnterior, Estado estadoNuevo,
                                             String actor, String motivo, LocalDateTime ocurridoEn) {
        HistorialEstado h = nuevo(estadoAnterior, estadoNuevo, actor, motivo, ocurridoEn);
        h.id = id;
        return h;
    }

    public Long getId() {
        return id;
    }

    public Estado getEstadoAnterior() {
        return estadoAnterior;
    }

    public Estado getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getActor() {
        return actor;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
