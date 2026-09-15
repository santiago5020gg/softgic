# CLAUDE.md

Guía para Claude Code al trabajar en este repositorio.

## Proyecto

**Plataforma de gestión de solicitudes operacionales** — prueba técnica Full Stack
(cliente de gobierno). Arquitectura hexagonal, microservicios, eventos y microfrontends.
El objetivo es demostrar decisiones de ingeniería con un sistema **ejecutable**
(`docker compose up --build`), no una plataforma productiva completa.

## Estado actual

**Fase 0 completa y verde.** Monorepo, orquestación con Docker, SQL Server con migraciones
Flyway (modelo operacional + esquema estrella), y los dos microservicios backend arrancando
sanos (`/actuator/health` = UP). Ver `README.md` → Roadmap para las fases siguientes.

## Estándares (skills) — autoridad de código

Bajo `.claude/skills/` viven los estándares del proyecto. **Invoca la skill que
corresponda antes de agregar o cambiar código:**

- **`backend-hexagonal-standard`** — Java 21 + Spring Boot, hexagonal, `core` sin
  framework, servicios agregados, Flyway (SQL Server), Outbox + consumo idempotente, TDD.
  Para cualquier cambio bajo `backend/`.
- **`frontend-component-standard`** — React 19 + TS + MUI 7 + Emotion, Redux Toolkit, Zod,
  Module Federation/Rspack (shell + remoto), container/presentational, TDD. Bajo `frontend/`.
- **`security-audit`** — encuentra y reporta vulnerabilidades (Keycloak/JWT, RBAC en
  servidor, secretos, deps). Solo detección; reporte en `security/`.
- **`qa-playwright-mcp`** — QA como usuario real por el navegador (Playwright MCP).
- **`validate-before-push`** — puerta pre-push que corre los validadores aplicables.

## Estructura del repo

- `backend/svc-solicitudes/` — hexagonal completo (`com.gov.solicitudes`): escritura y
  consulta operacional. `core` (domain/dto/mapper/ports/usecase) +
  `infrastructure/adapter/{in,out}` + `application/config`.
- `backend/svc-indicadores/` — CQRS de lectura (`com.gov.indicadores`): consume eventos,
  esquema estrella, consultas agregadas.
- `frontend/` — `shell` (host) + `mf-remoto` (federado). *(Fase 4)*
- `keycloak/` — realm/clients/roles exportados. *(Fase 3)*
- `helm/`, `.gitlab-ci.yml` — despliegue y pipeline. *(Fase 6)*
- `karate/` — suite E2E de API (A1, A3, recorrido). *(Fase 5)*
- `docs/` — C4, secuencia, ADRs, OpenAPI, contratos de evento, modelo de datos.
- `docker-compose.yml` — SQL Server + init BD + Kafka + Keycloak + servicios.

## Arquitectura (hexagonal / puertos & adaptadores)

- El `core` es business logic pura: **no depende de Spring, JPA ni `infrastructure`** —
  solo de interfaces (puertos). Las dependencias apuntan hacia adentro. Es lo más evaluado.
- La **máquina de estados** (REGISTRADA→EN_ATENCION→RESUELTA→CERRADA, sin saltos) vive en
  el dominio (`Estado.puedeTransicionarA`); un salto inválido es excepción de dominio (A4).
- **Eventos confiables:** patrón **Outbox** (evento escrito en la misma tx de negocio; un
  relay lo publica a Kafka). El consumidor de indicadores es **idempotente** (A5).
- **Concurrencia:** optimistic locking (`@Version`) evita doble asignación (A2).

## Datos & config

- **Flyway es dueño del esquema** (`ddl-auto: validate`) sobre **SQL Server**. Cambios de
  esquema = nueva migración `V*__*.sql` en `src/main/resources/db/migration` (T-SQL).
- Config vía env (ver `.env.example`): `SPRING_DATASOURCE_*`, `SPRING_KAFKA_*`,
  `MSSQL_SA_PASSWORD`, puertos de host `SVC_*_PORT`. **Sin secretos en el repo.**

## Comandos

```bash
cp .env.example .env
docker compose up --build          # levanta todo el stack
curl http://localhost:8080/actuator/health   # (o el override SVC_SOLICITUDES_PORT)
curl http://localhost:8082/actuator/health
docker compose down                # detener (down -v borra datos)
```

No hay Maven local: el build de cada servicio ocurre dentro de su Dockerfile multi-stage.

## Git — Gitflow (obligatorio)

`feature/<desc>` desde `develop` → merge `--no-ff` a `develop` → borra la rama → `master`
solo cuando esté verificado. Nunca commits directos a `master`. Conventional Commits
(`feat:`, `fix:`, `docs:`, `chore:`, `test:`, `refactor:`). En PowerShell, mensajes
multilínea con `git commit -F`.

## Metodología — TDD

Red → Green → Refactor estricto: primero el test que falla, luego el mínimo código, luego
refactor. El `core` se prueba sin contexto de Spring. Entrega incremental por fases.

## Idioma

Todo el repo en **español**: código, comentarios, documentación, commits y ramas.
