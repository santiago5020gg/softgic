package com.gov.solicitudes.core.ports.in;

import com.gov.solicitudes.core.dto.CrearSolicitudCommand;
import com.gov.solicitudes.core.dto.FiltroSolicitudes;
import com.gov.solicitudes.core.dto.PageResult;
import com.gov.solicitudes.core.dto.SolicitudDetalleDto;
import com.gov.solicitudes.core.dto.SolicitudResumenDto;
import com.gov.solicitudes.core.dto.TransicionCommand;

/** Puerto de entrada: casos de uso sobre solicitudes (servicio agregado). */
public interface SolicitudServicePort {

    SolicitudDetalleDto registrar(CrearSolicitudCommand command);

    PageResult<SolicitudResumenDto> listar(FiltroSolicitudes filtro, int page, int size);

    SolicitudDetalleDto detalle(Long id);

    SolicitudDetalleDto tomar(Long id, String analista, String correlationId);

    SolicitudDetalleDto resolver(Long id, String observacion, String actor, String correlationId);

    SolicitudDetalleDto devolver(Long id, String motivo, String actor, String correlationId);

    SolicitudDetalleDto cerrar(Long id, String actor, String correlationId);

    /** Despacha una transición unificada hacia el método concreto según la acción. */
    default SolicitudDetalleDto transicionar(Long id, TransicionCommand cmd) {
        return switch (cmd.accion()) {
            case TOMAR -> tomar(id, cmd.actor(), cmd.correlationId());
            case RESOLVER -> resolver(id, cmd.observacion(), cmd.actor(), cmd.correlationId());
            case DEVOLVER -> devolver(id, cmd.motivo(), cmd.actor(), cmd.correlationId());
            case CERRAR -> cerrar(id, cmd.actor(), cmd.correlationId());
        };
    }
}
