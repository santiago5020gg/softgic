-- V2 — Datos semilla NO sensibles (regla del reto: nada real/clasificado/personal).
-- Catálogo de categorías (incluye una inactiva para probar el filtro "activas").

INSERT INTO categoria (nombre, activo) VALUES
    (N'Soporte técnico',        1),
    (N'Coordinación operativa', 1),
    (N'Infraestructura',        1),
    (N'Gestión documental',     1),
    (N'Accesos y permisos',     0);
