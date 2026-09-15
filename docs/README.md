# docs/

Documentación de arquitectura de la **Plataforma de gestión de solicitudes operacionales**.

## Diagramas de arquitectura

- [`c4-contexto.md`](./c4-contexto.md) — C4 Nivel 1 (Contexto): usuarios por rol
  (SOLICITANTE, ANALISTA, SUPERVISOR) y el sistema externo Keycloak alrededor de la plataforma.
- [`c4-contenedores.md`](./c4-contenedores.md) — C4 Nivel 2 (Contenedores): shell, microfrontend,
  `svc-solicitudes`, `svc-indicadores`, SQL Server, Kafka y Keycloak, con protocolos
  (REST /api/v1, OIDC/PKCE, JDBC, eventos Kafka).
- [`secuencia-flujo-principal.md`](./secuencia-flujo-principal.md) — diagrama de secuencia del
  recorrido completo de una solicitud (REGISTRADA → EN_ATENCION → RESUELTA → CERRADA), con el
  patrón Outbox y el consumo idempotente de eventos por `svc-indicadores`.

Todos los diagramas están escritos en **Mermaid dentro de Markdown**, por lo que renderizan
directamente en GitHub y GitLab.

## Otras referencias

- [`adr/`](./adr/) — Architecture Decision Records: decisiones de diseño y sus alternativas
  consideradas.
- Contratos de API (OpenAPI), contratos de evento y modelo de datos: se agregan de forma
  incremental conforme avanzan las fases del proyecto (ver `README.md` raíz → Roadmap).
