# Plataforma de gestión de solicitudes operacionales

Prueba técnica Full Stack — arquitectura hexagonal, microservicios, eventos y microfrontends.

> **Estado: completo y ejecutable.** Dos microservicios backend hexagonales (SQL Server,
> Flyway, Outbox→Kafka), seguridad Keycloak (OIDC/PKCE + RBAC), frontend shell + microfrontend
> federado (Rspack/Module Federation), suite Karate, Helm y pipeline GitLab CI. Todo arranca
> con `docker compose up --build`. Ver [Roadmap](#roadmap) para el detalle por fases.

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
| 5 | Pruebas: JUnit/JaCoCo, Vitest/Storybook, Karate (A1,A3,recorrido) | ✅ |
| 6 | Helm + GitLab CI + diagramas + documentación final (A7) | ✅ completada |

## Frontend (Fase 4)

- **shell** (host): `http://localhost:5173` — React 19 + MUI 7, Redux Toolkit, Zod,
  login OIDC **Authorization Code + PKCE** (client `solicitudes-shell`, token en memoria).
  Vistas: bandeja con filtros, crear, detalle con línea de tiempo y acciones por rol,
  resumen analítico.
- **mf-remoto** (microfrontend federado): `http://localhost:3002` — expone
  `IndicadoresPanel`, integrado en el shell por **Module Federation** (Rspack) y también
  ejecutable *standalone*.

## Documentación

- Arquitectura C4: [contexto](docs/c4-contexto.md) · [contenedores](docs/c4-contenedores.md) ·
  [secuencia del flujo](docs/secuencia-flujo-principal.md)
- Decisiones (ADR): [`docs/adr/`](docs/adr/)
- Contratos: [HTTP / OpenAPI](docs/openapi.md) · [eventos](docs/event-contracts/)
- [Modelo de datos](docs/modelo-de-datos.md) (operacional vs. estrella)

## Pruebas

- **Backend:** JUnit 5 + Mockito (dominio/casos de uso sin Spring); reporte **JaCoCo**
  en `target/site/jacoco` (aggregate `Solicitud` ~99%; el global no se infla con
  getters/entidades JPA).
- **Frontend:** Vitest + Testing Library + MSW; **Storybook** para componentes de `ui/`.
- **E2E de API — Karate** (`karate/`): cubre A1, A3 y el recorrido
  REGISTRADA→EN_ATENCION→RESUELTA contra el stack real con tokens de Keycloak.
  ```bash
  docker run --rm --network prueba-softgic_ps-net \
    -v "$PWD/karate:/app" -w /app maven:3.9-eclipse-temurin-21 mvn -B test
  ```

## CI/CD

Pipeline **GitLab CI** ([`.gitlab-ci.yml`](.gitlab-ci.yml)) con etapas verificables:
`build` → `test` → `coverage` (JaCoCo) → `package` (4 imágenes Docker) → `helm`
(lint + template) → `deploy` (**manual**). Promoción por ambientes (dev→staging→prod);
los secretos van en variables de CI protegidas, **nunca** en el repo.

**Kubernetes/Helm:** chart en [`helm/`](helm/) con Deployment, Service, ConfigMap/Secret
references, probes y recursos. No se exige clúster; el despliegue se explica y queda manual.

## Observabilidad

- Health/readiness/liveness de Spring Boot Actuator en `/actuator/health` (usados por los
  healthchecks de `docker-compose` y las probes de Helm).
- Trazabilidad de negocio: `historial_estado` (auditoría por transición) y `correlationId`
  en el sobre de cada evento.

## Limitaciones y trabajo pendiente

- Generación de `codigo` legible con `count()+1` (suficiente para el reto; en producción
  sería una secuencia/patrón por año para evitar colisiones bajo alta concurrencia).
- Frontera transaccional del caso de uso declarada en el controller (`@Transactional`);
  una alternativa es un decorador transaccional del puerto de entrada.
- Relay del Outbox por *polling* (`@Scheduled`); en producción podría evolucionar a CDC.
- Filtrado "el SOLICITANTE solo ve sus solicitudes" no está restringido a nivel de fila
  (RBAC por operación sí está aplicado).
- El despliegue real a Kubernetes queda documentado pero no ejecutado (sin clúster).

## Convenciones

- **Gitflow**: `feature/*` → `develop` → `master`. Conventional Commits.
- **TDD**: Red → Green → Refactor. El `core` se prueba sin Spring.
- **Idioma**: el código y la documentación técnica de este repo están en español,
  acorde al contexto del reto.
