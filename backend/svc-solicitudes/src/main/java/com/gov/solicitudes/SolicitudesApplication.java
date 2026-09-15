package com.gov.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Servicio de Solicitudes.
 *
 * <p>Arquitectura hexagonal: esta clase (composición Spring Boot) vive en la capa de
 * arranque; el núcleo de negocio ({@code core}) permanece libre de framework.
 */
@SpringBootApplication
public class SolicitudesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolicitudesApplication.class, args);
    }
}
