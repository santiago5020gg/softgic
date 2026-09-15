# Contratos de evento

Eventos de dominio publicados por **svc-solicitudes** y consumidos por **svc-indicadores**.

## Transporte

- **Broker:** Kafka.
- **Topic:** `solicitudes.eventos` (todos los tipos comparten topic).
- **Key:** `aggregateId` (el código legible de la solicitud, p. ej. `SOL-2026-000001`).
  Garantiza orden por agregado dentro de la partición.
- **Value:** JSON con el sobre estándar (abajo). Serialización String/String.

## Sobre del evento

Todos los eventos comparten esta envoltura:

| Campo | Tipo | Descripción |
|---|---|---|
| `eventId` | UUID | Identificador único del evento. **Clave de idempotencia**. |
| `type` | string | Tipo del evento (ver lista). |
| `aggregateId` | string | Código de la solicitud. |
| `version` | int | Versión del contrato del evento. |
| `correlationId` | UUID | Correlación entre eventos de un mismo flujo. |
| `occurredAt` | ISO-8601 | Momento en que ocurrió el hecho de negocio. |
| `payload` | objeto | Datos específicos del tipo (ver ejemplos). |

## Tipos y ejemplos

| Tipo | Archivo | Transición |
|---|---|---|
| `SolicitudRegistrada` | [SolicitudRegistrada.json](SolicitudRegistrada.json) | → REGISTRADA |
| `SolicitudTomada` | [SolicitudTomada.json](SolicitudTomada.json) | REGISTRADA → EN_ATENCION |
| `SolicitudResuelta` | [SolicitudResuelta.json](SolicitudResuelta.json) | EN_ATENCION → RESUELTA |
| `SolicitudDevuelta` | [SolicitudDevuelta.json](SolicitudDevuelta.json) | RESUELTA → EN_ATENCION |
| `SolicitudCerrada` | [SolicitudCerrada.json](SolicitudCerrada.json) | RESUELTA → CERRADA |

## Garantías

- **Publicación confiable (Outbox):** el evento se escribe en la tabla `outbox` dentro de
  la MISMA transacción que el cambio de negocio; un relay lo publica después del commit.
  Nunca se publica un evento cuya transacción no confirmó (evita pérdidas por *dual write*).
- **Entrega at-least-once:** el relay puede reintentar; por eso el consumidor es idempotente.
- **Consumo idempotente:** `svc-indicadores` registra cada `eventId` en `processed_event`;
  si un evento llega dos veces, no se cuenta doble (escenario A5).
