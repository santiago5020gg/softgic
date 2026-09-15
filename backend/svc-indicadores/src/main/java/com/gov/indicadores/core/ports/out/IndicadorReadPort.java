package com.gov.indicadores.core.ports.out;

import com.gov.indicadores.core.dto.ConteoCategoria;
import com.gov.indicadores.core.dto.ConteoEstado;
import com.gov.indicadores.core.dto.PuntoTendencia;
import java.util.List;

/** Puerto de salida: lectura agregada del modelo analítico. */
public interface IndicadorReadPort {

    List<ConteoEstado> conteoPorEstado();

    List<ConteoCategoria> conteoPorCategoria();

    List<PuntoTendencia> tendenciaDiaria();
}
