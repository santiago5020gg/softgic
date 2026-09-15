package com.gov.indicadores.core.usecase;

import com.gov.indicadores.core.dto.PuntoTendencia;
import com.gov.indicadores.core.dto.ResumenDto;
import com.gov.indicadores.core.ports.in.IndicadorServicePort;
import com.gov.indicadores.core.ports.out.IndicadorReadPort;
import java.util.List;

/** Caso de uso de consultas agregadas (framework-free). */
public class IndicadorService implements IndicadorServicePort {

    private final IndicadorReadPort read;

    public IndicadorService(IndicadorReadPort read) {
        this.read = read;
    }

    @Override
    public ResumenDto resumen() {
        return new ResumenDto(read.conteoPorEstado(), read.conteoPorCategoria());
    }

    @Override
    public List<PuntoTendencia> tendencia() {
        return read.tendenciaDiaria();
    }
}
