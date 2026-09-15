package com.gov.solicitudes.core.domain;

import com.gov.solicitudes.core.exception.TransicionInvalidaException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate root del proceso. Encapsula la máquina de estados: toda transición pasa por
 * {@link #transicionar} que valida contra {@link Estado#puedeTransicionarA} y, si el salto
 * es inválido, lanza {@link TransicionInvalidaException} (escenario A4). Cada transición
 * deja una entrada de {@link HistorialEstado} y acumula un {@link EventoDominio} que el
 * caso de uso recupera con {@link #pullEventos()} para escribirlo en el outbox.
 */
public class Solicitud {

    private Long id;
    private String codigo;
    private String asunto;
    private String descripcion;
    private Long categoriaId;
    private Prioridad prioridad;
    private Estado estado;
    private String solicitante;
    private String analista;
    private long version;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private final List<Observacion> observaciones = new ArrayList<>();
    private final List<HistorialEstado> historial = new ArrayList<>();
    private final List<EventoDominio> eventos = new ArrayList<>();

    private Solicitud() {
    }

    /** Registra una solicitud nueva en estado REGISTRADA con su historial y evento inicial. */
    public static Solicitud registrar(String codigo, String asunto, String descripcion,
                                      Long categoriaId, Prioridad prioridad, String solicitante,
                                      String correlationId) {
        Solicitud s = new Solicitud();
        s.codigo = codigo;
        s.asunto = asunto;
        s.descripcion = descripcion;
        s.categoriaId = categoriaId;
        s.prioridad = prioridad;
        s.solicitante = solicitante;
        s.estado = Estado.REGISTRADA;
        LocalDateTime now = LocalDateTime.now();
        s.creadoEn = now;
        s.actualizadoEn = now;
        s.version = 0;
        s.historial.add(HistorialEstado.nuevo(null, Estado.REGISTRADA, solicitante, "Registro inicial", now));
        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo", codigo);
        payload.put("categoriaId", categoriaId);
        payload.put("prioridad", prioridad.name());
        payload.put("estado", s.estado.name());
        payload.put("solicitante", solicitante);
        s.eventos.add(EventoDominio.de("SolicitudRegistrada", codigo, correlationId, payload));
        return s;
    }

    /** Reconstruye un aggregate existente desde persistencia (sin generar eventos ni historial). */
    public static Solicitud rehidratar(Long id, String codigo, String asunto, String descripcion,
                                       Long categoriaId, Prioridad prioridad, Estado estado,
                                       String solicitante, String analista, long version,
                                       LocalDateTime creadoEn, LocalDateTime actualizadoEn,
                                       List<Observacion> observaciones, List<HistorialEstado> historial) {
        Solicitud s = new Solicitud();
        s.id = id;
        s.codigo = codigo;
        s.asunto = asunto;
        s.descripcion = descripcion;
        s.categoriaId = categoriaId;
        s.prioridad = prioridad;
        s.estado = estado;
        s.solicitante = solicitante;
        s.analista = analista;
        s.version = version;
        s.creadoEn = creadoEn;
        s.actualizadoEn = actualizadoEn;
        if (observaciones != null) {
            s.observaciones.addAll(observaciones);
        }
        if (historial != null) {
            s.historial.addAll(historial);
        }
        return s;
    }

    /** Un analista toma la solicitud: REGISTRADA -> EN_ATENCION. */
    public void tomar(String analista, String correlationId) {
        transicionar(Estado.EN_ATENCION, analista, null);
        this.analista = analista;
        eventos.add(EventoDominio.de("SolicitudTomada", codigo, correlationId,
                evento("analista", analista)));
    }

    /** El analista agrega una observación y resuelve: EN_ATENCION -> RESUELTA. */
    public void resolver(String observacionTexto, String actor, String correlationId) {
        if (observacionTexto != null && !observacionTexto.isBlank()) {
            observaciones.add(Observacion.nueva(observacionTexto, actor, LocalDateTime.now()));
        }
        transicionar(Estado.RESUELTA, actor, null);
        eventos.add(EventoDominio.de("SolicitudResuelta", codigo, correlationId,
                evento("actor", actor)));
    }

    /** El supervisor devuelve una solicitud resuelta: RESUELTA -> EN_ATENCION. */
    public void devolver(String motivo, String actor, String correlationId) {
        transicionar(Estado.EN_ATENCION, actor, motivo);
        Map<String, Object> payload = evento("actor", actor);
        payload.put("motivo", motivo == null ? "" : motivo);
        eventos.add(EventoDominio.de("SolicitudDevuelta", codigo, correlationId, payload));
    }

    /** El supervisor cierra una solicitud resuelta: RESUELTA -> CERRADA. */
    public void cerrar(String actor, String correlationId) {
        transicionar(Estado.CERRADA, actor, null);
        eventos.add(EventoDominio.de("SolicitudCerrada", codigo, correlationId,
                evento("actor", actor)));
    }

    private void transicionar(Estado destino, String actor, String motivo) {
        if (!estado.puedeTransicionarA(destino)) {
            throw new TransicionInvalidaException(
                    "Transición inválida de " + estado + " a " + destino
                            + " para la solicitud " + codigo);
        }
        LocalDateTime now = LocalDateTime.now();
        historial.add(HistorialEstado.nuevo(estado, destino, actor, motivo, now));
        this.estado = destino;
        this.actualizadoEn = now;
    }

    private Map<String, Object> evento(String claveActor, String valorActor) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo", codigo);
        payload.put("estado", estado.name());
        payload.put(claveActor, valorActor);
        return payload;
    }

    /** Devuelve y limpia los eventos acumulados (los publica el caso de uso). */
    public List<EventoDominio> pullEventos() {
        List<EventoDominio> copia = List.copyOf(eventos);
        eventos.clear();
        return copia;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getAnalista() {
        return analista;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public List<Observacion> getObservaciones() {
        return List.copyOf(observaciones);
    }

    public List<HistorialEstado> getHistorial() {
        return List.copyOf(historial);
    }
}
