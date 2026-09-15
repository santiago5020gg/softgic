# Diagrama de secuencia — Flujo principal de una solicitud

Recorrido completo de una solicitud operacional a través de sus cuatro estados
(REGISTRADA → EN_ATENCION → RESUELTA → CERRADA), sin saltarse ninguno (regla de dominio A4).
Cada transición se persiste en la misma transacción de negocio junto con un evento en la
tabla **Outbox** de `svc-solicitudes`; un relay publica esos eventos a **Kafka**, y
**svc-indicadores** los consume de forma **idempotente** para mantener su esquema estrella
actualizado. Los tres roles (SOLICITANTE, ANALISTA, SUPERVISOR) participan cada uno en el
paso que le corresponde.

```mermaid
sequenceDiagram
    autonumber
    actor Sol as Solicitante
    actor Ana as Analista
    actor Sup as Supervisor
    participant FE as Shell / mf-remoto
    participant SS as svc-solicitudes
    participant DB as SQL Server (solicitudes + outbox)
    participant Relay as Outbox Relay
    participant K as Kafka
    participant SI as svc-indicadores
    participant DBI as SQL Server (indicadores)

    rect rgb(235, 245, 255)
    note over Sol,DBI: 1. Registro de la solicitud
    Sol->>FE: Completa y envía formulario de solicitud
    FE->>SS: POST /api/v1/solicitudes (JWT: rol SOLICITANTE)
    SS->>SS: Crea solicitud en estado REGISTRADA
    SS->>DB: INSERT solicitud + INSERT evento "SolicitudRegistrada" en outbox (misma tx)
    DB-->>SS: OK (commit)
    SS-->>FE: 201 Created (solicitud REGISTRADA)
    FE-->>Sol: Confirma número de solicitud

    Relay->>DB: Lee eventos pendientes del outbox
    DB-->>Relay: Evento "SolicitudRegistrada"
    Relay->>K: Publica "SolicitudRegistrada"
    Relay->>DB: Marca evento como publicado
    K-->>SI: Consume "SolicitudRegistrada"
    SI->>SI: ¿Evento ya procesado? (idempotencia por id de evento)
    SI->>DBI: Upsert hecho/dimensión (si no procesado)
    end

    rect rgb(235, 255, 235)
    note over Ana,DBI: 2. Toma de la solicitud
    Ana->>FE: Toma la solicitud asignada
    FE->>SS: PATCH /api/v1/solicitudes/{id}/tomar (JWT: rol ANALISTA)
    SS->>SS: Valida transición REGISTRADA -> EN_ATENCION (optimistic locking @Version)
    SS->>DB: UPDATE estado=EN_ATENCION + INSERT evento "SolicitudTomada" en outbox (misma tx)
    DB-->>SS: OK (commit)
    SS-->>FE: 200 OK (solicitud EN_ATENCION)

    Relay->>DB: Lee eventos pendientes del outbox
    DB-->>Relay: Evento "SolicitudTomada"
    Relay->>K: Publica "SolicitudTomada"
    K-->>SI: Consume "SolicitudTomada"
    SI->>SI: ¿Evento ya procesado? (idempotencia)
    SI->>DBI: Actualiza indicadores (tiempos de atención)
    end

    rect rgb(255, 250, 230)
    note over Ana,DBI: 3. Resolución de la solicitud
    Ana->>FE: Registra resolución de la solicitud
    FE->>SS: PATCH /api/v1/solicitudes/{id}/resolver (JWT: rol ANALISTA)
    SS->>SS: Valida transición EN_ATENCION -> RESUELTA
    SS->>DB: UPDATE estado=RESUELTA + INSERT evento "SolicitudResuelta" en outbox (misma tx)
    DB-->>SS: OK (commit)
    SS-->>FE: 200 OK (solicitud RESUELTA)

    Relay->>DB: Lee eventos pendientes del outbox
    DB-->>Relay: Evento "SolicitudResuelta"
    Relay->>K: Publica "SolicitudResuelta"
    K-->>SI: Consume "SolicitudResuelta"
    SI->>SI: ¿Evento ya procesado? (idempotencia)
    SI->>DBI: Actualiza indicadores (tiempos de resolución)
    end

    rect rgb(255, 235, 235)
    note over Sup,DBI: 4. Cierre de la solicitud
    Sup->>FE: Revisa y cierra la solicitud resuelta
    FE->>SS: PATCH /api/v1/solicitudes/{id}/cerrar (JWT: rol SUPERVISOR)
    SS->>SS: Valida transición RESUELTA -> CERRADA
    SS->>DB: UPDATE estado=CERRADA + INSERT evento "SolicitudCerrada" en outbox (misma tx)
    DB-->>SS: OK (commit)
    SS-->>FE: 200 OK (solicitud CERRADA)

    Relay->>DB: Lee eventos pendientes del outbox
    DB-->>Relay: Evento "SolicitudCerrada"
    Relay->>K: Publica "SolicitudCerrada"
    K-->>SI: Consume "SolicitudCerrada"
    SI->>SI: ¿Evento ya procesado? (idempotencia)
    SI->>DBI: Cierra el ciclo del hecho (tiempos totales, KPIs de cierre)
    end

    Sup->>FE: Consulta indicadores agregados
    FE->>SI: GET /api/v1/indicadores (JWT: rol SUPERVISOR)
    SI->>DBI: Consulta esquema estrella
    DBI-->>SI: Resultados agregados
    SI-->>FE: 200 OK (indicadores)
    FE-->>Sup: Muestra tablero de indicadores
```

## Notas

- **Sin saltos de estado (A4):** cualquier intento de transición inválida (por ejemplo,
  REGISTRADA → RESUELTA directamente) es rechazado por el dominio (`Estado.puedeTransicionarA`)
  con una excepción de dominio, antes de tocar la base de datos.
- **Outbox como garantía de entrega:** el evento se escribe en la misma transacción que el
  cambio de estado, así que nunca hay un cambio de estado sin su evento correspondiente (ni
  viceversa). El relay reintenta la publicación a Kafka hasta confirmarla.
- **Idempotencia en `svc-indicadores` (A5):** Kafka garantiza *at-least-once*, por lo que el
  consumidor deduplica por identificador de evento antes de aplicar el efecto sobre el
  esquema estrella, evitando duplicar hechos ante redelivery o reprocesamiento.
- **Concurrencia (A2):** el `@Version` (optimistic locking) en `svc-solicitudes` evita que
  dos analistas tomen la misma solicitud simultáneamente; el segundo intento recibe un
  conflicto de concurrencia.
