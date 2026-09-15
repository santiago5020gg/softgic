# Plataforma de gestión de solicitudes operacionales

Prueba técnica Full Stack — arquitectura hexagonal, microservicios, eventos y microfrontends.

> **Estado actual: Fase 0 (fundaciones).** Monorepo, orquestación local con Docker y los
> dos microservicios backend que arrancan sanos contra SQL Server con migraciones aplicadas.
> El resto de fases (dominio + API, eventos/Outbox, Keycloak, frontend, Karate, Helm, CI)
> se construye de forma incremental. Ver [Roadmap](#roadmap).

## Arquitectura (resumen)

- **svc-solicitudes** — escritura y consulta operacional. Arquitectura hexagonal
  (`core` libre de framework; `infrastructure/adapter/{in,out}`; `application/config`).
- **svc-indicadores** — modelo de lectura (CQRS) alimentado por eventos; esquema estrella.
- **SQL Server** — persistencia; el esquema es propiedad de **Flyway** (Hibernate en `validate`).
- **Kafka** — bus de eventos (Outbox → publicación confiable → consumo idempotente).
- **Keycloak** — identidad (OIDC / Authorization Code + PKCE en el front; Resource Server en el back).

Diagramas C4 y de secuencia en [`docs/`](docs/). Decisiones en [`docs/adr/`](docs/adr/).

## Prerrequisitos

- Docker + Docker Compose
- (Opcional para desarrollo sin contenedores) JDK 21

## Ejecución local

```bash
cp .env.example .env      # ajusta credenciales locales si quieres
docker compose up --build
```

Arranca en orden: SQL Server → creación de bases (`solicitudes`, `indicadores`) →
Kafka + Keycloak → los dos servicios.

### Puertos

| Servicio         | URL                                             |
|------------------|-------------------------------------------------|
| svc-solicitudes  | http://localhost:8080/actuator/health           |
| svc-indicadores  | http://localhost:8082/actuator/health           |
| Keycloak (admin) | http://localhost:8081                           |
| SQL Server       | localhost:1433 (sa / valor de `MSSQL_SA_PASSWORD`) |
| Kafka            | interno en `kafka:9092` (red del compose)        |

### Validación del estado

```bash
curl http://localhost:8080/actuator/health   # {"status":"UP"}
curl http://localhost:8082/actuator/health   # {"status":"UP"}
```

## Roadmap

| Fase | Contenido | Estado |
|------|-----------|--------|
| 0 | Monorepo, compose, SQL Server + migraciones, 2 servicios que arrancan | ✅ actual |
| 1 | Dominio + casos de uso + API REST/OpenAPI (camino feliz, A1/A4) | ⏳ |
| 2 | Outbox + Kafka + consumidor idempotente + indicadores (A2/A5) | ⏳ |
| 3 | Keycloak: realm, PKCE en front, Resource Server + RBAC (A3) | ⏳ |
| 4 | Frontend: shell + microfrontend (Module Federation/Rspack), MUI, Redux, Zod (A6) | ⏳ |
| 5 | Pruebas: JUnit/JaCoCo, Vitest/Storybook, Karate (A1,A3,recorrido) | ⏳ |
| 6 | Helm + GitLab CI + diagramas + documentación final (A7) | ⏳ |

## Convenciones

- **Gitflow**: `feature/*` → `develop` → `master`. Conventional Commits.
- **TDD**: Red → Green → Refactor. El `core` se prueba sin Spring.
- **Idioma**: el código y la documentación técnica de este repo están en español,
  acorde al contexto del reto.
