# Architecture Decision Records (ADR)

Registro de decisiones de arquitectura de la **Plataforma de gestión de solicitudes
operacionales** (prueba técnica Full Stack — cliente de gobierno). Cada ADR documenta el
contexto, la decisión tomada, las alternativas consideradas y sus consecuencias
(incluyendo trade-offs negativos aceptados conscientemente).

## Índice

| ADR | Título | Estado |
|-----|--------|--------|
| [ADR-0001](ADR-0001-arquitectura-hexagonal.md) | Arquitectura hexagonal (puertos y adaptadores) con `core` libre de framework | Aceptada |
| [ADR-0002](ADR-0002-microservicios-cqrs.md) | Dos microservicios con CQRS — escritura (Solicitudes) separada de lectura analítica (Indicadores) | Aceptada |
| [ADR-0003](ADR-0003-outbox-consumo-idempotente.md) | Publicación confiable de eventos con patrón Outbox + consumo idempotente | Aceptada |
| [ADR-0004](ADR-0004-kafka-broker-eventos.md) | Kafka como broker de eventos (frente a RabbitMQ) | Aceptada |
| [ADR-0005](ADR-0005-sqlserver-flyway-modelos.md) | SQL Server con Flyway como dueño del esquema; modelo operacional normalizado + modelo analítico en estrella | Aceptada |
| [ADR-0006](ADR-0006-keycloak-seguridad.md) | Seguridad con Keycloak — Authorization Code + PKCE en el front, Resource Server + RBAC en el back | Aceptada |
| [ADR-0007](ADR-0007-frontend-module-federation.md) | Frontend — shell + microfrontend con Module Federation sobre Rspack; MUI 7, Redux Toolkit, Zod | Aceptada |

## Convención

- Un archivo por ADR: `ADR-000X-<slug>.md`.
- Formato fijo: Estado, Fecha, Contexto, Decisión, Alternativas consideradas, Consecuencias.
- Los ADRs no se editan retroactivamente para "corregir" una decisión ya tomada: si cambia
  el contexto o la decisión, se crea un ADR nuevo que referencia y supera al anterior.
- Idioma: español, acorde a la convención del resto del repositorio.
