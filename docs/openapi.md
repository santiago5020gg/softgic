# Contratos HTTP (OpenAPI)

## svc-solicitudes

- **Especificación OpenAPI:** [`openapi-solicitudes.json`](openapi-solicitudes.json)
  (exportada de `GET /v3/api-docs` del servicio en ejecución).
- **Swagger UI (interactivo):** `http://localhost:8080/swagger-ui.html`
  (o el puerto de host configurado, p. ej. `:18080`).

| Método | Ruta | Descripción | Autorización |
|---|---|---|---|
| POST | `/api/v1/solicitudes` | Crear solicitud → 201, estado REGISTRADA | SOLICITANTE |
| GET | `/api/v1/solicitudes` | Listar (paginado + filtro `estado`, `categoriaId`) | autenticado |
| GET | `/api/v1/solicitudes/{id}` | Detalle con línea de tiempo | autenticado |
| POST | `/api/v1/solicitudes/{id}/asignaciones` | Tomar → EN_ATENCION | ANALISTA |
| POST | `/api/v1/solicitudes/{id}/transiciones` | Resolver / devolver / cerrar | ANALISTA (resolver) / SUPERVISOR (devolver, cerrar) |
| GET | `/api/v1/categorias` | Catálogo de categorías activas | autenticado |

Códigos de error coherentes: `201` creación, `200` operación, `400` validación,
`401` sin token, `403` rol insuficiente (A3), `404` no encontrada, `409` transición
inválida (A4) / conflicto de bloqueo optimista (A2).

## svc-indicadores

Modelo de lectura alimentado por eventos (no expone springdoc). Base `http://localhost:8082`.

| Método | Ruta | Descripción | Autorización |
|---|---|---|---|
| GET | `/api/v1/indicadores/resumen` | Solicitudes por estado y por categoría | autenticado |
| GET | `/api/v1/indicadores/tendencia` | Tendencia diaria | autenticado |

Respuesta de `/resumen`:

```json
{
  "porEstado":   [{ "estado": "REGISTRADA", "total": 3 }],
  "porCategoria":[{ "categoriaId": 1, "total": 2 }]
}
```

Respuesta de `/tendencia`:

```json
[{ "fecha": "2026-09-15", "total": 7 }]
```
