# C4 — Nivel 2: Contenedores

Despliegue de la vista de contexto en los contenedores que componen el stack local
(`docker compose up --build`): el **shell** React (host de Module Federation) que carga en
tiempo de ejecución el **microfrontend** remoto; dos servicios backend con arquitectura
hexagonal (**svc-solicitudes** para escritura/consulta operacional, **svc-indicadores** para
consultas agregadas de solo lectura); **SQL Server** como persistencia de ambos (bases de
datos separadas: `solicitudes` e `indicadores`); **Kafka** como bus de eventos entre ambos
servicios vía patrón Outbox; y **Keycloak** como IdP compartido.

```mermaid
C4Container
    title Diagrama de Contenedores — Plataforma de gestión de solicitudes operacionales

    Person(usuario, "Usuario", "SOLICITANTE / ANALISTA / SUPERVISOR")

    System_Boundary(plataforma, "Plataforma de gestión de solicitudes operacionales") {
        Container(shell, "Shell", "React 19 + TS + MUI 7, Rspack (host Module Federation)", "Aplicación contenedora: layout, ruteo, sesión OIDC/PKCE, carga el microfrontend remoto")
        Container(mfRemoto, "mf-remoto", "React 19 + TS + MUI 7, Redux Toolkit, Zod, Rspack (remoto Module Federation)", "Pantallas de negocio: registrar/listar/tomar/resolver/cerrar solicitudes e indicadores")

        Container(svcSolicitudes, "svc-solicitudes", "Java 21 + Spring Boot, hexagonal", "Escritura y consulta operacional de solicitudes. Máquina de estados del dominio. Publica eventos vía Outbox")
        Container(svcIndicadores, "svc-indicadores", "Java 21 + Spring Boot, CQRS de lectura", "Consume eventos de dominio de forma idempotente y mantiene un esquema estrella para consultas agregadas")

        ContainerDb(sqlServer, "SQL Server", "SQL Server 2022, esquema por Flyway", "Bases de datos 'solicitudes' (operacional + tabla outbox) e 'indicadores' (esquema estrella)")
        ContainerQueue(kafka, "Kafka", "Apache Kafka 3.9 (KRaft, sin ZooKeeper)", "Bus de eventos de dominio: SolicitudRegistrada, SolicitudTomada, SolicitudResuelta, SolicitudCerrada")
    }

    System_Ext(keycloak, "Keycloak", "OIDC / Authorization Code + PKCE, Resource Server (JWT)")

    Rel(usuario, shell, "Usa la aplicación", "HTTPS")
    Rel(shell, mfRemoto, "Carga en tiempo de ejecución", "Module Federation (JS remoto)")

    Rel(shell, keycloak, "Login redirect / logout", "OIDC Authorization Code + PKCE")
    Rel(mfRemoto, svcSolicitudes, "CRUD y transiciones de solicitudes", "REST /api/v1, JWT Bearer")
    Rel(mfRemoto, svcIndicadores, "Consultas agregadas de indicadores", "REST /api/v1, JWT Bearer")

    Rel(svcSolicitudes, keycloak, "Valida el JWT (Resource Server)", "OIDC / JWKS")
    Rel(svcIndicadores, keycloak, "Valida el JWT (Resource Server)", "OIDC / JWKS")

    Rel(svcSolicitudes, sqlServer, "Lee/escribe solicitudes y tabla outbox", "JDBC (base 'solicitudes')")
    Rel(svcIndicadores, sqlServer, "Lee/escribe esquema estrella", "JDBC (base 'indicadores')")

    Rel(svcSolicitudes, kafka, "Publica eventos de dominio (relay del Outbox)", "Kafka Producer")
    Rel(kafka, svcIndicadores, "Consume eventos de dominio (idempotente)", "Kafka Consumer")

    UpdateLayoutConfig("landscape")
```

## Notas

- **Shell vs. microfrontend:** el shell es el host de Module Federation (layout, ruteo,
  sesión); `mf-remoto` es el remoto que expone las pantallas de negocio y se integra en
  tiempo de ejecución (no en tiempo de build). Ambos son contenedores separables aunque se
  desplieguen desde el mismo monorepo.
- **Outbox → Kafka:** `svc-solicitudes` no publica a Kafka dentro de la misma transacción de
  negocio; escribe el evento en una tabla `outbox` (misma base `solicitudes`, misma
  transacción) y un relay lo publica a Kafka de forma confiable (at-least-once). El detalle
  del flujo está en [`secuencia-flujo-principal.md`](./secuencia-flujo-principal.md).
- **Idempotencia en el consumidor:** `svc-indicadores` procesa cada evento a lo sumo una vez
  a nivel de efecto (deduplicación por identificador de evento), tolerando reintentos y
  redelivery de Kafka.
- **Bases de datos separadas:** `solicitudes` (modelo operacional + outbox) e `indicadores`
  (esquema estrella) viven en la misma instancia de SQL Server pero son bases independientes;
  no hay acceso cruzado directo entre servicios a nivel de datos, solo vía eventos.
- **Puertos locales** (`docker-compose.yml`): shell/mf-remoto (Fase 4, sin puerto aún fijado),
  `svc-solicitudes` en `SVC_SOLICITUDES_PORT` (default 8080), `svc-indicadores` en
  `SVC_INDICADORES_PORT` (default 8082), Keycloak en 8081, SQL Server en 1433, Kafka interno
  en `kafka:9092` (red `ps-net`, sin puerto expuesto al host).
