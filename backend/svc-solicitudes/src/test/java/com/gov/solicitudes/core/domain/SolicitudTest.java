package com.gov.solicitudes.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gov.solicitudes.core.exception.TransicionInvalidaException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del aggregate {@link Solicitud} — dominio puro, sin Spring. */
class SolicitudTest {

    private Solicitud registrarBase() {
        return Solicitud.registrar("SOL-2026-000001", "Asunto", "Descripción",
                1L, Prioridad.MEDIA, "solicitante1", "corr-1");
    }

    @Test
    @DisplayName("registrar deja la solicitud en REGISTRADA con historial y evento inicial (A1)")
    void registrarCreaEstadoInicial() {
        Solicitud s = registrarBase();

        assertThat(s.getEstado()).isEqualTo(Estado.REGISTRADA);
        assertThat(s.getHistorial()).hasSize(1);
        assertThat(s.getHistorial().get(0).getEstadoAnterior()).isNull();
        assertThat(s.getHistorial().get(0).getEstadoNuevo()).isEqualTo(Estado.REGISTRADA);

        List<EventoDominio> eventos = s.pullEventos();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).type()).isEqualTo("SolicitudRegistrada");
        assertThat(s.pullEventos()).isEmpty(); // pull limpia los eventos
    }

    @Test
    @DisplayName("camino feliz REGISTRADA -> EN_ATENCION -> RESUELTA -> CERRADA")
    void caminoFeliz() {
        Solicitud s = registrarBase();
        s.pullEventos(); // descarto el evento inicial

        s.tomar("analista1", "corr-2");
        assertThat(s.getEstado()).isEqualTo(Estado.EN_ATENCION);
        assertThat(s.getAnalista()).isEqualTo("analista1");

        s.resolver("Se atendió correctamente", "analista1", "corr-3");
        assertThat(s.getEstado()).isEqualTo(Estado.RESUELTA);
        assertThat(s.getObservaciones()).hasSize(1);

        s.cerrar("supervisor1", "corr-4");
        assertThat(s.getEstado()).isEqualTo(Estado.CERRADA);

        assertThat(s.getHistorial()).hasSize(4); // registro + 3 transiciones
        List<EventoDominio> eventos = s.pullEventos();
        assertThat(eventos).extracting(EventoDominio::type)
                .containsExactly("SolicitudTomada", "SolicitudResuelta", "SolicitudCerrada");
    }

    @Test
    @DisplayName("A4: una transición inválida (REGISTRADA -> CERRADA) lanza TransicionInvalidaException")
    void transicionInvalidaSaltoDirecto() {
        Solicitud s = registrarBase();
        assertThatThrownBy(() -> s.cerrar("supervisor1", "corr"))
                .isInstanceOf(TransicionInvalidaException.class);
        assertThat(s.getEstado()).isEqualTo(Estado.REGISTRADA); // no cambió
    }

    @Test
    @DisplayName("A4: resolver desde REGISTRADA (sin pasar por EN_ATENCION) se rechaza")
    void resolverDesdeRegistradaInvalido() {
        Solicitud s = registrarBase();
        assertThatThrownBy(() -> s.resolver("x", "analista1", "corr"))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    @Test
    @DisplayName("El supervisor puede devolver una solicitud resuelta a EN_ATENCION")
    void devolucion() {
        Solicitud s = registrarBase();
        s.tomar("analista1", "c");
        s.resolver("listo", "analista1", "c");
        s.pullEventos();

        s.devolver("Falta evidencia", "supervisor1", "c");
        assertThat(s.getEstado()).isEqualTo(Estado.EN_ATENCION);
        assertThat(s.pullEventos()).extracting(EventoDominio::type).containsExactly("SolicitudDevuelta");
    }
}
