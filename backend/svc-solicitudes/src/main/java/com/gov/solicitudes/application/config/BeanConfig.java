package com.gov.solicitudes.application.config;

import com.gov.solicitudes.core.mapper.CategoriaMapper;
import com.gov.solicitudes.core.mapper.SolicitudMapper;
import com.gov.solicitudes.core.ports.in.CatalogoServicePort;
import com.gov.solicitudes.core.ports.in.SolicitudServicePort;
import com.gov.solicitudes.core.ports.out.CategoriaRepositoryPort;
import com.gov.solicitudes.core.ports.out.EventPublisherPort;
import com.gov.solicitudes.core.ports.out.GeneradorCodigoPort;
import com.gov.solicitudes.core.ports.out.SolicitudRepositoryPort;
import com.gov.solicitudes.core.usecase.CatalogoService;
import com.gov.solicitudes.core.usecase.SolicitudService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raíz de composición: cablea explícitamente los servicios de core (clases planas,
 * libres de framework) con los adaptadores de salida detectados por Spring.
 */
@Configuration
public class BeanConfig {

    @Bean
    public SolicitudMapper solicitudMapper() {
        return new SolicitudMapper();
    }

    @Bean
    public CategoriaMapper categoriaMapper() {
        return new CategoriaMapper();
    }

    @Bean
    public SolicitudServicePort solicitudServicePort(SolicitudRepositoryPort solicitudRepository,
                                                     CategoriaRepositoryPort categoriaRepository,
                                                     EventPublisherPort eventPublisher,
                                                     GeneradorCodigoPort generadorCodigo,
                                                     SolicitudMapper solicitudMapper) {
        return new SolicitudService(solicitudRepository, categoriaRepository,
                eventPublisher, generadorCodigo, solicitudMapper);
    }

    @Bean
    public CatalogoServicePort catalogoServicePort(CategoriaRepositoryPort categoriaRepository,
                                                   CategoriaMapper categoriaMapper) {
        return new CatalogoService(categoriaRepository, categoriaMapper);
    }
}
