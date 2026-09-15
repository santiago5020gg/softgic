-- V1 — Modelo analítico tipo estrella del Servicio de Indicadores (SQL Server).
-- Tabla de hechos: transiciones/eventos. Dimensiones: fecha, categoría, estado, actor/rol.
-- Se evita replicar datos personales innecesarios (solo el rol del actor).

CREATE TABLE dim_fecha (
    id    INT  NOT NULL PRIMARY KEY,   -- formato AAAAMMDD (ej. 20260915)
    fecha DATE NOT NULL,
    anio  INT  NOT NULL,
    mes   INT  NOT NULL,
    dia   INT  NOT NULL
);

CREATE TABLE dim_categoria (
    id     BIGINT        NOT NULL PRIMARY KEY,   -- mismo id que en el operacional
    nombre NVARCHAR(120) NOT NULL
);

CREATE TABLE dim_estado (
    id     INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    estado NVARCHAR(20)      NOT NULL UNIQUE
);

CREATE TABLE dim_actor_rol (
    id  INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    rol NVARCHAR(20)      NOT NULL UNIQUE
);

CREATE TABLE fact_transicion (
    id               BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    dim_fecha_id     INT    NOT NULL,
    dim_categoria_id BIGINT NULL,
    dim_estado_id    INT    NOT NULL,
    dim_actor_rol_id INT    NULL,
    conteo           INT    NOT NULL CONSTRAINT df_fact_conteo DEFAULT 1,
    CONSTRAINT fk_fact_fecha     FOREIGN KEY (dim_fecha_id)     REFERENCES dim_fecha(id),
    CONSTRAINT fk_fact_categoria FOREIGN KEY (dim_categoria_id) REFERENCES dim_categoria(id),
    CONSTRAINT fk_fact_estado    FOREIGN KEY (dim_estado_id)    REFERENCES dim_estado(id),
    CONSTRAINT fk_fact_actor     FOREIGN KEY (dim_actor_rol_id) REFERENCES dim_actor_rol(id)
);
CREATE INDEX ix_fact_fecha  ON fact_transicion(dim_fecha_id);
CREATE INDEX ix_fact_estado ON fact_transicion(dim_estado_id);

-- Idempotencia del consumidor: si un evento llega dos veces, no se cuenta doble (A5).
CREATE TABLE processed_event (
    event_id     NVARCHAR(50) NOT NULL PRIMARY KEY,
    processed_at DATETIME2    NOT NULL CONSTRAINT df_processed_at DEFAULT SYSUTCDATETIME()
);

-- Semilla de dimensiones estables (los estados posibles del proceso).
INSERT INTO dim_estado (estado) VALUES (N'REGISTRADA'), (N'EN_ATENCION'), (N'RESUELTA'), (N'CERRADA');
INSERT INTO dim_actor_rol (rol) VALUES (N'SOLICITANTE'), (N'ANALISTA'), (N'SUPERVISOR');
