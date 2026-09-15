package com.gov.solicitudes.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI solicitudesOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Servicio de Solicitudes API")
                .version("v1")
                .description("Registro, asignación y seguimiento de solicitudes operacionales."));
    }
}
