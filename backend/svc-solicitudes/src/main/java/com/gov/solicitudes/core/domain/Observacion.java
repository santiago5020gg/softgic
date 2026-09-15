package com.gov.solicitudes.core.domain;

import java.time.LocalDateTime;

/** Observación registrada por un analista sobre una solicitud. */
public class Observacion {

    private Long id;
    private String texto;
    private String actor;
    private LocalDateTime creadoEn;

    private Observacion() {
    }

    /** Nueva observación (aún sin persistir). */
    public static Observacion nueva(String texto, String actor, LocalDateTime creadoEn) {
        Observacion o = new Observacion();
        o.texto = texto;
        o.actor = actor;
        o.creadoEn = creadoEn;
        return o;
    }

    /** Reconstruye una observación existente desde persistencia. */
    public static Observacion rehidratar(Long id, String texto, String actor, LocalDateTime creadoEn) {
        Observacion o = new Observacion();
        o.id = id;
        o.texto = texto;
        o.actor = actor;
        o.creadoEn = creadoEn;
        return o;
    }

    public Long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public String getActor() {
        return actor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
