# Modelo de datos

Dos modelos separados, cada uno en su base de datos SQL Server, propiedad de **Flyway**
(`ddl-auto=validate`, Hibernate nunca crea/altera tablas).

---

## 1. Modelo operacional (normalizado) — base `solicitudes`

Servicio **svc-solicitudes**. Optimizado para escritura consistente y trazabilidad.
Migraciones: `backend/svc-solicitudes/src/main/resources/db/migration/V1__baseline_operacional.sql`
(+ `V2__seed_categorias.sql`).

```mermaid
erDiagram
    categoria ||--o{ solicitud : clasifica
    solicitud ||--o{ observacion : tiene
    solicitud ||--o{ historial_estado : registra

    categoria {
        bigint id PK
        nvarchar nombre
        bit activo
    }
    solicitud {
        bigint id PK
        nvarchar codigo UK "identificador legible SOL-AAAA-NNNNNN"
        nvarchar asunto
        nvarchar descripcion
        bigint categoria_id FK
        nvarchar prioridad "BAJA|MEDIA|ALTA"
        nvarchar estado "REGISTRADA|EN_ATENCION|RESUELTA|CERRADA"
        nvarchar solicitante
        nvarchar analista
        bigint version "optimistic locking (A2)"
        datetime2 creado_en
        datetime2 actualizado_en
    }
    observacion {
        bigint id PK
        bigint solicitud_id FK
        nvarchar texto
        nvarchar actor
        datetime2 creado_en
    }
    historial_estado {
        bigint id PK
        bigint solicitud_id FK
        nvarchar estado_anterior
        nvarchar estado_nuevo
        nvarchar actor
        nvarchar motivo
        datetime2 ocurrido_en
    }
    outbox {
        bigint id PK
        nvarchar event_id UK "clave de idempotencia"
        nvarchar aggregate_id
        nvarchar type
        int version
        nvarchar correlation_id
        nvarchar payload "JSON"
        datetime2 occurred_at
        bit published
        datetime2 published_at
    }
```

- **`historial_estado`** da trazabilidad completa: cada transición conserva actor, fecha y motivo.
- **`outbox`** implementa la publicación confiable de eventos (patrón Outbox).

---

## 2. Modelo analítico (estrella) — base `indicadores`

Servicio **svc-indicadores** (CQRS de lectura). Optimizado para consultas agregadas.
Migraciones: `backend/svc-indicadores/src/main/resources/db/migration/V1__baseline_estrella.sql`
(+ `V2__read_model_estado_actual.sql`). Evita replicar datos personales innecesarios
(solo el rol del actor, no su identidad).

```mermaid
erDiagram
    dim_fecha ||--o{ fact_transicion : cuando
    dim_categoria ||--o{ fact_transicion : de
    dim_estado ||--o{ fact_transicion : hacia
    dim_actor_rol ||--o{ fact_transicion : por

    fact_transicion {
        bigint id PK
        int dim_fecha_id FK
        bigint dim_categoria_id FK
        int dim_estado_id FK
        int dim_actor_rol_id FK
        int conteo
    }
    dim_fecha {
        int id PK "AAAAMMDD"
        date fecha
        int anio
        int mes
        int dia
    }
    dim_categoria {
        bigint id PK
        nvarchar nombre
    }
    dim_estado {
        int id PK
        nvarchar estado
    }
    dim_actor_rol {
        int id PK
        nvarchar rol
    }
    solicitud_actual {
        nvarchar aggregate_id PK "estado ACTUAL por solicitud"
        nvarchar estado
        bigint categoria_id
        datetime2 registrado_en
    }
    processed_event {
        nvarchar event_id PK "idempotencia del consumidor (A5)"
        datetime2 processed_at
    }
```

- **`fact_transicion`** (tabla de hechos) alimenta la **tendencia diaria**.
- **`solicitud_actual`** mantiene el estado vigente de cada solicitud → conteos exactos
  **por estado** y **por categoría** (modelo de lectura CQRS).
- **`processed_event`** garantiza el consumo idempotente (un `eventId` no se cuenta dos veces).

---

## Diferencia operacional vs. analítico

| Aspecto | Operacional (`solicitudes`) | Analítico (`indicadores`) |
|---|---|---|
| Propósito | Escritura consistente, reglas de negocio | Consultas agregadas de solo lectura |
| Normalización | Normalizado (3FN) | Estrella (hechos + dimensiones) |
| Fuente de la verdad | Sí (comandos) | Derivado de eventos |
| Actualización | Transaccional | Asíncrona (consumo de eventos Kafka) |
