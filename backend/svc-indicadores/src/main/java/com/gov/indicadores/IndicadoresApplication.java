package com.gov.indicadores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Servicio de Indicadores.
 *
 * <p>Consume los eventos publicados por el Servicio de Solicitudes y mantiene un
 * modelo de lectura (esquema estrella) para consultas agregadas: solicitudes por
 * estado, por categoría y tendencia diaria.
 */
@SpringBootApplication
public class IndicadoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndicadoresApplication.class, args);
    }
}
