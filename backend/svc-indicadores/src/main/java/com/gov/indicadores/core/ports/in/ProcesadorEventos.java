package com.gov.indicadores.core.ports.in;

import com.gov.indicadores.core.dto.EventoEntrante;

/** Puerto de entrada: procesa un evento de solicitud actualizando el modelo de lectura. */
public interface ProcesadorEventos {

    void procesar(EventoEntrante evento);
}
