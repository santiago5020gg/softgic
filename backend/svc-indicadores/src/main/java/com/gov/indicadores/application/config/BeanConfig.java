package com.gov.indicadores.application.config;

import com.gov.indicadores.core.ports.in.IndicadorServicePort;
import com.gov.indicadores.core.ports.in.ProcesadorEventos;
import com.gov.indicadores.core.ports.out.IndicadorReadPort;
import com.gov.indicadores.core.ports.out.ModeloLecturaPort;
import com.gov.indicadores.core.usecase.IndicadorService;
import com.gov.indicadores.core.usecase.ProcesarEventoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Raíz de composición: cablea los casos de uso (clases planas) con sus puertos. */
@Configuration
public class BeanConfig {

    @Bean
    public ProcesadorEventos procesadorEventos(ModeloLecturaPort modelo) {
        return new ProcesarEventoService(modelo);
    }

    @Bean
    public IndicadorServicePort indicadorServicePort(IndicadorReadPort read) {
        return new IndicadorService(read);
    }
}
