package com.gov.solicitudes.infrastructure.adapter.in;

import com.gov.solicitudes.core.domain.Estado;
import com.gov.solicitudes.core.dto.CrearSolicitudCommand;
import com.gov.solicitudes.core.dto.FiltroSolicitudes;
import com.gov.solicitudes.core.dto.PageResult;
import com.gov.solicitudes.core.dto.SolicitudDetalleDto;
import com.gov.solicitudes.core.dto.SolicitudResumenDto;
import com.gov.solicitudes.core.dto.TransicionCommand;
import com.gov.solicitudes.core.ports.in.SolicitudServicePort;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter de entrada REST para solicitudes.
 *
 * <p>Frontera transaccional del caso de uso: cada operación de escritura corre en una
 * transacción (persistencia + outbox atómicos). Las lecturas usan {@code readOnly}.
 *
 * <p>PROVISIONAL (hasta Keycloak, Fase 3): el actor y el rol se leen de los headers
 * {@code X-Usuario} y {@code X-Rol}; aún no hay enforcement estricto de rol en servidor.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudServicePort service;

    public SolicitudController(SolicitudServicePort service) {
        this.service = service;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<SolicitudDetalleDto> crear(
            @Valid @RequestBody CrearSolicitudRequest req,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        SolicitudDetalleDto dto = service.registrar(new CrearSolicitudCommand(
                req.asunto(), req.descripcion(), req.categoriaId(), req.prioridad(), usuario, correlationId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/v1/solicitudes/" + dto.id())
                .body(dto);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public PageResult<SolicitudResumenDto> listar(
            @RequestParam(required = false) Estado estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(new FiltroSolicitudes(estado, categoriaId), page, size);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public SolicitudDetalleDto detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    @PostMapping("/{id}/asignaciones")
    @Transactional
    public SolicitudDetalleDto tomar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return service.tomar(id, usuario, correlationId);
    }

    @PostMapping("/{id}/transiciones")
    @Transactional
    public SolicitudDetalleDto transicionar(
            @PathVariable Long id,
            @Valid @RequestBody TransicionRequest req,
            @RequestHeader("X-Usuario") String usuario,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return service.transicionar(id, new TransicionCommand(
                req.accion(), req.observacion(), req.motivo(), usuario, correlationId));
    }
}
