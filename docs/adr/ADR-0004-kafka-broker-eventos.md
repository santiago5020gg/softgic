# ADR-0004: Kafka como broker de eventos (frente a RabbitMQ)
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

El flujo Outbox → publicación → consumo idempotente (ADR-0003) necesita un broker de
mensajería/eventos entre svc-solicitudes y svc-indicadores. Los eventos de dominio
(cambios de estado de solicitud) son, en esencia, un log de hechos ocurridos que
svc-indicadores reproduce para construir su modelo de lectura; no son simples "tareas" a
procesar una vez y descartar.

## Decisión

Se usa **Apache Kafka** como broker de eventos entre los microservicios, desplegado en modo
KRaft de nodo único (`apache/kafka:3.9.0`, sin Zookeeper) en `docker-compose.yml`, con un
tópico por tipo de evento de dominio relevante.

## Alternativas consideradas

- **RabbitMQ**: más simple de operar para colas de tareas punto a punto, con enrutamiento
  flexible (exchanges) y menor huella de recursos. Sería una alternativa razonable si el
  caso de uso fuera solo "entregar cada mensaje a un worker". Se descarta como opción
  principal porque:
  - Kafka retiene el log de eventos por más tiempo (o indefinidamente) y permite que un
    nuevo consumidor (p. ej. un futuro servicio de auditoría) reprocese el historial completo
    desde el offset que necesite; RabbitMQ, orientado a colas, descarta el mensaje una vez
    confirmado por todos los consumidores existentes.
  - El modelo de particiones + offsets de Kafka encaja naturalmente con el patrón Outbox y
    con el requisito de consumo idempotente por evento (identificador + offset).
  - Kafka es el estándar de facto para arquitecturas orientadas a eventos a la escala que
    una plataforma de gobierno con múltiples sistemas consumidores probablemente requerirá
    a futuro (aunque esta prueba técnica solo tenga un consumidor).
- **Bus de eventos in-process (sin broker externo)**: eliminaría infraestructura, pero
  rompe el desacoplamiento entre servicios (exigiría que ambos compartan proceso) e
  invalida el requisito explícito del reto de tener microservicios independientes
  comunicados por eventos.

## Consecuencias

**Positivas**
- Retención/replay del log de eventos: útil para reconstruir el esquema en estrella de
  indicadores desde cero si fuera necesario, o para incorporar nuevos consumidores sin tocar
  al productor.
- Particionamiento y consumer groups dan una ruta natural de escalado horizontal para
  svc-indicadores.
- Ecosistema maduro y ampliamente conocido; encaja con el resto del stack orientado a
  eventos (Outbox, idempotencia por offset/clave).

**Negativas / trade-offs**
- Mayor complejidad operativa que RabbitMQ: más conceptos (tópicos, particiones, consumer
  groups, offsets, retención) y una huella de recursos más pesada, notable incluso en modo
  KRaft de un solo nodo para desarrollo local.
- Sin gestión de reintentos/DLQ nativa tan directa como en RabbitMQ (exchanges de
  dead-letter); en Kafka esto se construye explícitamente (tópicos de reintento/DLQ,
  manejo de errores en el consumidor).
- Para el volumen y alcance de esta prueba técnica, Kafka es más potencia de la
  estrictamente necesaria; se acepta el costo porque el broker de eventos y el patrón Outbox
  son parte de lo explícitamente evaluado en el reto.
