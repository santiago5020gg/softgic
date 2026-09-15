-- V2 — Modelo de lectura del estado ACTUAL de cada solicitud.
-- Permite conteos exactos por estado y por categoría (una fila por solicitud),
-- complementando la tabla de hechos (fact_transicion) que sirve la tendencia diaria.

CREATE TABLE solicitud_actual (
    aggregate_id  NVARCHAR(50) NOT NULL PRIMARY KEY,   -- código legible de la solicitud
    estado        NVARCHAR(20) NOT NULL,               -- estado vigente
    categoria_id  BIGINT       NULL,
    registrado_en DATETIME2    NOT NULL
);
CREATE INDEX ix_solicitud_actual_estado    ON solicitud_actual(estado);
CREATE INDEX ix_solicitud_actual_categoria ON solicitud_actual(categoria_id);
