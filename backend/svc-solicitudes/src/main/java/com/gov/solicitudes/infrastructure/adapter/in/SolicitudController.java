package com.gov.solicitudes.infrastructure.adapter.in;

import com.gov.solicitudes.core.domain.Estado;
import com.gov.solicitudes.core.dto.AccionTransicion;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter de entrada REST para solicitudes.
 *
 * <p>Frontera transaccional del caso de uso: cada operación de escritura corre en una
 * transacción (persistencia + outbox atómicos). Las lecturas usan {@code readOnly}.
 *
 * <p>Seguridad (Fase 3): el actor es el {@code preferred_username} del JWT de Keycloak y la
 * autorización por rol se aplica en servidor (RBAC). El escenario A3 (rol insuficiente → 403)
 * se cubre con {@code @PreAuthorize} y la verificación por acción en {@link #transicionar}.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    private final SolicitudServicePort service;

    public SolicitudController(SolicitudServicePort service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('SOLICITANTE')")
    @Transactional
    public ResponseEntity<SolicitudDetalleDto> crear(
            @Valid @RequestBody CrearSolicitudRequest req,
            JwtAuthenticationToken auth) {
        SolicitudDetalleDto dto = service.registrar(new CrearSolicitudCommand(
                req.asunto(), req.descripcion(), req.categoriaId(), req.prioridad(),
                auth.getName(), correlationId(auth)));
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
            @RequestParam(defaultValue = "20") int size,
            JwtAuthenticationToken auth) {
        // Regla del reto: el SOLICITANTE solo consulta SUS solicitudes; analista/supervisor ven todas.
        String soloDe = veTodas(auth) ? null : auth.getName();
        return service.listar(new FiltroSolicitudes(estado, categoriaId, soloDe), page, size);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public SolicitudDetalleDto detalle(@PathVariable Long id, JwtAuthenticationToken auth) {
        SolicitudDetalleDto dto = service.detalle(id);
        // Un SOLICITANTE no puede ver el detalle de una solicitud ajena.
        if (!veTodas(auth) && !auth.getName().equals(dto.solicitante())) {
            throw new AccessDeniedException("No puede consultar solicitudes de otro usuario");
        }
        return dto;
    }

    @PostMapping("/{id}/asignaciones")
    @PreAuthorize("hasRole('ANALISTA')")
    @Transactional
    public SolicitudDetalleDto tomar(@PathVariable Long id, JwtAuthenticationToken auth) {
        return service.tomar(id, auth.getName(), correlationId(auth));
    }

    @PostMapping("/{id}/transiciones")
    @PreAuthorize("hasAnyRole('ANALISTA','SUPERVISOR')")
    @Transactional
    public SolicitudDetalleDto transicionar(
            @PathVariable Long id,
            @Valid @RequestBody TransicionRequest req,
            JwtAuthenticationToken auth) {
        // RBAC por acción (A3): resolver es del analista; devolver/cerrar, del supervisor.
        AccionTransicion accion = req.accion();
        if (accion == AccionTransicion.RESOLVER && !tieneRol(auth, "ROLE_ANALISTA")) {
            throw new AccessDeniedException("Resolver requiere el rol ANALISTA");
        }
        if ((accion == AccionTransicion.DEVOLVER || accion == AccionTransicion.CERRAR)
                && !tieneRol(auth, "ROLE_SUPERVISOR")) {
            throw new AccessDeniedException("Devolver/cerrar requiere el rol SUPERVISOR");
        }
        return service.transicionar(id, new TransicionCommand(
                accion, req.observacion(), req.motivo(), auth.getName(), correlationId(auth)));
    }

    private boolean tieneRol(JwtAuthenticationToken auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(rol));
    }

    /** Un ANALISTA o SUPERVISOR ve todas las solicitudes; el SOLICITANTE solo las suyas. */
    private boolean veTodas(JwtAuthenticationToken auth) {
        return tieneRol(auth, "ROLE_ANALISTA") || tieneRol(auth, "ROLE_SUPERVISOR");
    }

    private String correlationId(JwtAuthenticationToken auth) {
        return auth.getToken().getId();
    }
}
