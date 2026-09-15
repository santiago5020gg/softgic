package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "observacion")
public class ObservacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudEntity solicitud;

    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private String actor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected ObservacionEntity() {
    }

    public ObservacionEntity(String texto, String actor, LocalDateTime creadoEn) {
        this.texto = texto;
        this.actor = actor;
        this.creadoEn = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public void setSolicitud(SolicitudEntity solicitud) {
        this.solicitud = solicitud;
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
