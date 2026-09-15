package com.gov.indicadores.core.ports.in;

import com.gov.indicadores.core.dto.PuntoTendencia;
import com.gov.indicadores.core.dto.ResumenDto;
import java.util.List;

/** Puerto de entrada: consultas agregadas de indicadores. */
public interface IndicadorServicePort {

    ResumenDto resumen();

    List<PuntoTendencia> tendencia();
}
