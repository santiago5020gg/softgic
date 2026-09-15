# ADR-0003: Publicación confiable de eventos con patrón Outbox + consumo idempotente
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

svc-solicitudes debe notificar a svc-indicadores (y potencialmente a otros consumidores)
cada cambio relevante de estado de una solicitud. Si el evento se publicara directamente al
broker dentro (o inmediatamente después) de la transacción de base de datos, se abren dos
ventanas de fallo: (1) la transacción de negocio hace commit pero la publicación a Kafka
falla (red, broker caído) → el evento se pierde y los indicadores quedan desincronizados sin
que nadie lo note; o (2) el evento se publica pero la transacción de negocio hace rollback
después → se publica un evento de un cambio que nunca ocurrió. Además, Kafka (y el reinicio
de consumidores) puede entregar el mismo mensaje más de una vez (at-least-once delivery),
por lo que el receptor debe tolerar duplicados sin corromper el agregado de indicadores (A5
del reto).

## Decisión

Se implementa el patrón **Transactional Outbox**:

- Cuando svc-solicitudes ejecuta un caso de uso que cambia el estado de una solicitud,
  escribe en la **misma transacción** de base de datos tanto el cambio de estado como una
  fila en una tabla `outbox` (evento serializado, con un identificador único de evento).
- Un proceso relay (poller o CDC) lee la tabla `outbox` de forma asíncrona y publica cada
  evento pendiente a Kafka, marcándolo como publicado tras confirmación del broker.
- El consumidor en svc-indicadores procesa cada evento de forma **idempotente**: antes de
  aplicar el efecto (actualizar el esquema en estrella), verifica el identificador único del
  evento contra un registro de eventos ya procesados; si ya fue aplicado, lo descarta sin
  reaplicar.

Esto garantiza *at-least-once delivery* con *exactly-once effect* a nivel de aplicación.

## Alternativas consideradas

- **Publicar directamente al broker dentro de la transacción de negocio** (o justo después,
  sin outbox): es la opción más simple de implementar, pero no ofrece atomicidad entre el
  cambio de estado y la publicación del evento — es exactamente el problema que el patrón
  Outbox resuelve. Se descarta porque introduce pérdida o publicación fantasma de eventos,
  inaceptable para un sistema de gobierno donde los indicadores deben reflejar la realidad
  operacional.
- **Two-Phase Commit (2PC) entre la base de datos y Kafka**: técnicamente evita el problema,
  pero Kafka no soporta 2PC de forma nativa con transacciones XA, y añade acoplamiento y
  latencia significativos entre BD y broker. Se descarta por complejidad e inviabilidad
  práctica.
- **Debezium / CDC directo sobre el log de transacciones** en lugar de una tabla outbox
  explícita con poller: es una variante válida y más robusta en producción real, pero exige
  desplegar y operar Kafka Connect/Debezium, lo cual excede el alcance y el tiempo de esta
  prueba técnica. Se deja como evolución posible, no como decisión actual.
- **Consumidor no idempotente confiando en "exactly-once" de Kafka**: las garantías
  exactly-once de Kafka son costosas de configurar end-to-end (productor transaccional +
  consumidor transaccional) y no cubren duplicados originados fuera del broker (p. ej.
  reintentos del relay). Se prefiere idempotencia explícita en el consumidor, más simple de
  razonar y de probar.

## Consecuencias

**Positivas**
- Ningún evento se pierde: si el relay falla, el evento sigue en la tabla `outbox` y se
  reintenta.
- Ningún evento fantasma: solo se publica lo que efectivamente hizo commit en la
  transacción de negocio.
- El consumidor tolera duplicados y reintentos sin corromper el modelo de indicadores
  (requisito A5), lo cual también simplifica recuperarse de caídas del propio consumidor.

**Negativas / trade-offs**
- Latencia añadida: el evento no llega instantáneamente a Kafka, depende del intervalo de
  polling (o del lag de CDC) del relay — hay una ventana de consistencia eventual mayor que
  con publicación directa.
- Complejidad adicional: tabla `outbox`, proceso relay, tabla de eventos procesados en el
  consumidor, y limpieza/archivado periódico de ambas tablas para que no crezcan sin límite.
- Requiere disciplina de idempotencia en cada nuevo tipo de evento consumido; un olvido
  reintroduce el riesgo de duplicados aplicados dos veces.
