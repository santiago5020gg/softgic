package com.gov.solicitudes.infrastructure.adapter.out.persistence;

import com.gov.solicitudes.core.domain.Estado;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "historial_estado")
public class HistorialEstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudEntity solicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private Estado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private Estado estadoNuevo;

    @Column(nullable = false)
    private String actor;

    @Column
    private String motivo;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;

    protected HistorialEstadoEntity() {
    }

    public HistorialEstadoEntity(Estado estadoAnterior, Estado estadoNuevo, String actor,
                                 String motivo, LocalDateTime ocurridoEn) {
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.actor = actor;
        this.motivo = motivo;
        this.ocurridoEn = ocurridoEn;
    }

    public Long getId() {
        return id;
    }

    public void setSolicitud(SolicitudEntity solicitud) {
        this.solicitud = solicitud;
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
