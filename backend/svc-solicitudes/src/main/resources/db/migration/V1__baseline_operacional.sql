-- V1 — Modelo operacional normalizado del Servicio de Solicitudes (SQL Server).
-- Flyway es dueño del esquema; Hibernate corre con ddl-auto=validate.

CREATE TABLE categoria (
    id      BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nombre  NVARCHAR(120)        NOT NULL UNIQUE,
    activo  BIT                  NOT NULL CONSTRAINT df_categoria_activo DEFAULT 1
);

CREATE TABLE solicitud (
    id             BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    codigo         NVARCHAR(20)         NOT NULL UNIQUE,          -- identificador legible (SOL-2026-000123)
    asunto         NVARCHAR(200)        NOT NULL,
    descripcion    NVARCHAR(MAX)        NOT NULL,
    categoria_id   BIGINT               NOT NULL,
    prioridad      NVARCHAR(10)         NOT NULL,
    estado         NVARCHAR(20)         NOT NULL,
    solicitante    NVARCHAR(120)        NOT NULL,                 -- subject del JWT (no PII innecesaria)
    analista       NVARCHAR(120)        NULL,
    version        BIGINT               NOT NULL CONSTRAINT df_solicitud_version DEFAULT 0,  -- optimistic locking (A2)
    creado_en      DATETIME2            NOT NULL CONSTRAINT df_solicitud_creado DEFAULT SYSUTCDATETIME(),
    actualizado_en DATETIME2            NOT NULL CONSTRAINT df_solicitud_actualizado DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_solicitud_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    CONSTRAINT ck_solicitud_prioridad CHECK (prioridad IN ('BAJA','MEDIA','ALTA')),
    CONSTRAINT ck_solicitud_estado    CHECK (estado IN ('REGISTRADA','EN_ATENCION','RESUELTA','CERRADA'))
);
CREATE INDEX ix_solicitud_estado    ON solicitud(estado);
CREATE INDEX ix_solicitud_categoria ON solicitud(categoria_id);

CREATE TABLE observacion (
    id           BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    solicitud_id BIGINT               NOT NULL,
    texto        NVARCHAR(MAX)        NOT NULL,
    actor        NVARCHAR(120)        NOT NULL,
    creado_en    DATETIME2            NOT NULL CONSTRAINT df_observacion_creado DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_observacion_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud(id)
);
CREATE INDEX ix_observacion_solicitud ON observacion(solicitud_id);

-- Trazabilidad: cada transición conserva actor, fecha y motivo.
CREATE TABLE historial_estado (
    id              BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    solicitud_id    BIGINT               NOT NULL,
    estado_anterior NVARCHAR(20)         NULL,
    estado_nuevo    NVARCHAR(20)         NOT NULL,
    actor           NVARCHAR(120)        NOT NULL,
    motivo          NVARCHAR(500)        NULL,
    ocurrido_en     DATETIME2            NOT NULL CONSTRAINT df_historial_ocurrido DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_historial_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud(id)
);
CREATE INDEX ix_historial_solicitud ON historial_estado(solicitud_id);

-- Outbox transaccional: los eventos se escriben en la MISMA transacción de negocio
-- y un relay los publica luego al broker (evita publicar antes del commit).
CREATE TABLE outbox (
    id             BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    event_id       NVARCHAR(50)         NOT NULL UNIQUE,
    aggregate_id   NVARCHAR(50)         NOT NULL,
    type           NVARCHAR(60)         NOT NULL,   -- SolicitudRegistrada, SolicitudTomada, ...
    version        INT                  NOT NULL CONSTRAINT df_outbox_version DEFAULT 1,
    correlation_id NVARCHAR(50)         NOT NULL,
    payload        NVARCHAR(MAX)        NOT NULL,
    occurred_at    DATETIME2            NOT NULL CONSTRAINT df_outbox_occurred DEFAULT SYSUTCDATETIME(),
    published      BIT                  NOT NULL CONSTRAINT df_outbox_published DEFAULT 0,
    published_at   DATETIME2            NULL
);
-- Índice filtrado: el relay solo escanea los no publicados.
CREATE INDEX ix_outbox_unpublished ON outbox(published) WHERE published = 0;
