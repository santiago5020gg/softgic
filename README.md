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

## API (Fase 1 — svc-solicitudes)

OpenAPI / Swagger UI: `http://localhost:8080/swagger-ui.html` (o el puerto override).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/solicitudes` | Crear (201 → REGISTRADA) |
| GET | `/api/v1/solicitudes?estado=&categoriaId=&page=&size=` | Listar (paginado + filtro) |
| GET | `/api/v1/solicitudes/{id}` | Detalle con línea de tiempo |
| POST | `/api/v1/solicitudes/{id}/asignaciones` | Tomar (→ EN_ATENCION) |
| POST | `/api/v1/solicitudes/{id}/transiciones` | Resolver / devolver / cerrar |
| GET | `/api/v1/categorias` | Catálogo de categorías activas |

### Indicadores (Fase 2 — svc-indicadores, `http://localhost:8082`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/indicadores/resumen` | Solicitudes por estado y por categoría |
| GET | `/api/v1/indicadores/tendencia` | Tendencia diaria |

Alimentados por eventos Kafka (topic `solicitudes.eventos`) vía patrón **Outbox** en
`svc-solicitudes` y consumo **idempotente** (`processed_event`) en `svc-indicadores`.

## Seguridad (Fase 3 — Keycloak / OIDC)

Realm `solicitudes` (importado por `docker compose`). El backend actúa como **Resource
Server**: valida el JWT (issuer del host, JWKS por la red interna) y aplica **RBAC en
servidor**. El actor auditable es el `preferred_username` del token.

**Usuarios de prueba** (password `Password123!`):

| Usuario | Rol | Puede |
|---|---|---|
| `ana.solicitante` | SOLICITANTE | crear y consultar |
| `carlos.analista` | ANALISTA | tomar y resolver |
| `sofia.supervisor` | SUPERVISOR | consultar todas, devolver y cerrar |

**Obtener un token** (direct access grant, solo para pruebas):

```bash
curl -s -X POST http://localhost:8081/realms/solicitudes/protocol/openid-connect/token \
  -d grant_type=password -d client_id=solicitudes-shell \
  -d username=sofia.supervisor -d password=Password123!
# usar el access_token como:  -H "Authorization: Bearer <token>"
```

Sin token → `401`; rol insuficiente → `403` (p. ej. un SOLICITANTE cerrando → escenario A3).
El frontend usará Authorization Code + PKCE con el client público `solicitudes-shell` (Fase 4).

## Roadmap

| Fase | Contenido | Estado |
|------|-----------|--------|
| 0 | Monorepo, compose, SQL Server + migraciones, 2 servicios que arrancan | ✅ |
| 1 | Dominio + casos de uso + API REST/OpenAPI (camino feliz A1, rechazo A4), eventos a Outbox | ✅ |
| 2 | Relay Outbox→Kafka + consumidor idempotente + indicadores (A5) | ✅ |
| 3 | Keycloak: realm + Resource Server (JWT) + RBAC en servidor (A3) | ✅ |
| 4 | Frontend: shell + microfrontend (Module Federation/Rspack), MUI, Redux, Zod (A6) | ✅ |
| 5 | Pruebas: JUnit/JaCoCo, Vitest/Storybook, Karate (A1,A3,recorrido) | ✅ actual |
| 6 | Helm + GitLab CI + diagramas + documentación final (A7) | ⏳ |

## Convenciones

- **Gitflow**: `feature/*` → `develop` → `master`. Conventional Commits.
- **TDD**: Red → Green → Refactor. El `core` se prueba sin Spring.
- **Idioma**: el código y la documentación técnica de este repo están en español,
  acorde al contexto del reto.
