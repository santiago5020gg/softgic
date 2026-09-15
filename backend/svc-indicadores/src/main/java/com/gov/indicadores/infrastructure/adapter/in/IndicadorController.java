package com.gov.indicadores.infrastructure.adapter.in;

import com.gov.indicadores.core.dto.PuntoTendencia;
import com.gov.indicadores.core.dto.ResumenDto;
import com.gov.indicadores.core.ports.in.IndicadorServicePort;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adapter de entrada REST para las consultas de indicadores. */
@RestController
@RequestMapping("/api/v1/indicadores")
public class IndicadorController {

    private final IndicadorServicePort service;

    public IndicadorController(IndicadorServicePort service) {
        this.service = service;
    }

    /** Solicitudes por estado y por categoría (estado actual). */
    @GetMapping("/resumen")
    public ResumenDto resumen() {
        return service.resumen();
    }

    /** Tendencia diaria de transiciones. */
    @GetMapping("/tendencia")
    public List<PuntoTendencia> tendencia() {
        return service.tendencia();
    }
}
