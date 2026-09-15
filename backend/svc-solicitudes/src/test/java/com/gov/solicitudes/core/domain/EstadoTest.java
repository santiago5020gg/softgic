package com.gov.solicitudes.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prueba unitaria del dominio — sin contexto de Spring (el {@code core} es
 * framework-free). Cubre la máquina de estados que gobierna las transiciones válidas.
 */
class EstadoTest {

    @Test
    @DisplayName("Camino feliz: REGISTRADA -> EN_ATENCION -> RESUELTA -> CERRADA")
    void caminoFelizPermitido() {
        assertThat(Estado.REGISTRADA.puedeTransicionarA(Estado.EN_ATENCION)).isTrue();
        assertThat(Estado.EN_ATENCION.puedeTransicionarA(Estado.RESUELTA)).isTrue();
        assertThat(Estado.RESUELTA.puedeTransicionarA(Estado.CERRADA)).isTrue();
    }

    @Test
    @DisplayName("El supervisor puede devolver RESUELTA -> EN_ATENCION")
    void devolucionPermitida() {
        assertThat(Estado.RESUELTA.puedeTransicionarA(Estado.EN_ATENCION)).isTrue();
    }

    @Test
    @DisplayName("A4: una transición inválida (RESUELTA -> REGISTRADA) se rechaza")
    void transicionInvalidaRechazada() {
        assertThat(Estado.RESUELTA.puedeTransicionarA(Estado.REGISTRADA)).isFalse();
    }

    @Test
    @DisplayName("No se permiten saltos arbitrarios (REGISTRADA -> RESUELTA)")
    void saltoArbitrarioRechazado() {
        assertThat(Estado.REGISTRADA.puedeTransicionarA(Estado.RESUELTA)).isFalse();
    }

    @Test
    @DisplayName("Un estado terminal (CERRADA) no admite transiciones")
    void estadoTerminalSinSalidas() {
        for (Estado destino : Estado.values()) {
            assertThat(Estado.CERRADA.puedeTransicionarA(destino)).isFalse();
        }
    }
}
