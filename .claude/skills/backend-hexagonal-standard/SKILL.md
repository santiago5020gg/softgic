---
name: backend-hexagonal-standard
description: Use when writing, modifying, or reviewing Java/Spring Boot backend code in this project — adding or changing endpoints, use cases, ports, adapters, JPA entities, Flyway migrations (SQL Server), event publishing/consumption, or backend tests under backend/svc-solicitudes or backend/svc-indicadores. Enforces the hexagonal (ports & adapters) layout, aggregate services, reliable events via Outbox, and TDD.
---

# Backend Hexagonal Standard (este proyecto)

## Overview

Dos servicios Java 21 + Spring Boot bajo `backend/`:

- **`svc-solicitudes`** (`com.gov.solicitudes`) — hexagonal **completo**: escritura y
  consulta operacional. Es donde se evalúa con más peso la separación de capas.
- **`svc-indicadores`** (`com.gov.indicadores`) — hexagonal ligero (CQRS de lectura):
  consume eventos y expone consultas agregadas sobre el esquema estrella.

**Las dependencias siempre apuntan hacia adentro.** El `core` debe permanecer libre de
framework y ser unit-testeable sin contexto de Spring — es lo más evaluado del reto.

## Capas — dónde va el código

| Concern | Paquete |
|---|---|
| Dominio, DTOs, mappers, casos de uso | `core/` (sin Spring, sin JPA, sin imports de `infrastructure`) |
| Puertos de entrada (los llama el controlador) | `core/ports/in` — p.ej. `SolicitudServicePort`, `CatalogoServicePort` |
| Puertos de salida (los necesita el core) | `core/ports/out` — p.ej. `SolicitudRepositoryPort`, `CategoriaRepositoryPort`, `EventPublisherPort` |
| Controladores REST + `GlobalExceptionHandler` | `infrastructure/adapter/in` |
| Persistencia (JPA entities/repos, `*Adapter`, `*PersistenceMapper`), Outbox, seguridad | `infrastructure/adapter/out/{persistence,messaging,security}` |
| Raíz de composición (cablear adapters → core) | `application/config` |

## Reglas del core

1. **`core` es framework-free.** Nada de `org.springframework.*`, `jakarta.persistence.*`
   ni imports de `infrastructure` dentro de un `core`. Si un test de `core` necesita
   un contexto Spring, el diseño está mal.
2. **Servicios agregados, no una-clase-por-operación.** Una nueva operación sobre
   solicitudes va como método al `SolicitudService` existente (que implementa todo el
   `SolicitudServicePort`: registrar/listar/detalle/tomar/resolver/devolver/cerrar).
   NO crees `TomarSolicitudUseCase`, `CerrarSolicitudService`, etc. Añade el método al
   puerto y al servicio agregado.
3. **Nueva capacidad de salida = nuevo método en un `*Port` de `core/ports/out`**,
   implementado por su adapter en `infrastructure/adapter/out`. El core depende de la
   interfaz, nunca del adapter.
4. **La máquina de estados vive en el dominio.** Las transiciones válidas las decide el
   aggregate `Solicitud` (apoyado en `Estado.puedeTransicionarA`). Un salto inválido
   lanza una excepción de dominio (→ escenario A4), nunca un 500 genérico.
5. **El esquema es propiedad de Flyway** (`ddl-auto: validate`) sobre **SQL Server**.
   Cualquier cambio de esquema es una nueva migración `V*__*.sql` bajo
   `src/main/resources/db/migration` — nunca dejes que Hibernate cree/altere tablas.
   Usa T-SQL válido (`IDENTITY`, `NVARCHAR`, `DATETIME2`, `BIT`, índices filtrados).
6. **Eventos confiables vía Outbox.** El evento se escribe en la tabla `outbox` **en la
   misma transacción** que el cambio de negocio; un relay lo publica luego a Kafka. Nunca
   publiques al broker antes del commit. El consumidor de indicadores es **idempotente**
   (tabla `processed_event`, dedup por `eventId`) → escenario A5.
7. **Concurrencia:** el cambio de estado usa optimistic locking (`@Version`) para que dos
   analistas no tomen la misma solicitud → escenario A2.
8. Cablea cada adapter nuevo a su servicio de core explícitamente en `application/config`.

## Contratos y seguridad

- API REST documentada con **OpenAPI**; códigos HTTP coherentes; paginación y al menos
  un filtro en los listados. El controlador solo adapta HTTP → llama al puerto de entrada.
- El backend es **Resource Server**: valida el JWT de Keycloak y aplica autorización por
  rol también en el servidor (no solo en el front) → escenario A3. La identidad no es un
  puerto propio de token: se resuelve en `adapter/out/security` mapeando roles del JWT.

## Workflow (TDD + Gitflow)

- **TDD estricto Red → Green → Refactor.** Escribe primero el test que falla (unitario en
  `core` sin Spring; integración `*IT` en adapters con Testcontainers), luego el mínimo
  código para pasar, luego refactoriza. Sin código de producción sin un test que lo motive.
- **JUnit 5 + Mockito + JaCoCo.** Cobertura significativa de dominio/casos de uso, no
  inflada por getters triviales.
- **Gitflow:** `feature/<desc>` desde `develop`, merge con `--no-ff`, borra la rama.
  Nunca commitees a `master`. Conventional Commits. En PowerShell, mensajes multilínea
  con `git commit -F`.
- **Verifica antes de "listo":** el build corre dentro de Docker (`docker compose up
  --build`); localmente no hay Maven instalado, usa la imagen Maven del Dockerfile.

## Revisa dependencias en cada cambio

Al tocar un puerto o un método, rastrea el impacto entre capas: cada llamador e
implementador de ese puerto, actualiza el cableo en `application/config`, actualiza/añade
tests de todo lo afectado y confirma que no se filtró ninguna dependencia nueva al `core`.

## Checklist de autorrevisión

- [ ] Ninguna clase de `core` importa Spring / JPA / `infrastructure`.
- [ ] La operación nueva se añadió al servicio agregado (+ su `*ServicePort`), no como clase suelta.
- [ ] Todo cambio de BD es una nueva migración `V*` de Flyway (T-SQL válido para SQL Server).
- [ ] El evento se escribe en `outbox` dentro de la tx de negocio; el consumidor es idempotente.
- [ ] Transiciones de estado gobernadas por el dominio; salto inválido = excepción de dominio.
- [ ] Un test falla primero y luego pasa (TDD real); tests afectados actualizados.

## Errores comunes

| Error | Corrección |
|---|---|
| `XxxUseCase`/`XxxService` por operación | Añade el método al servicio agregado + su puerto |
| Lógica de negocio en el controlador | El controlador solo adapta HTTP → llama al puerto de entrada |
| `@Entity`/`@Autowired`/tipos Spring en `core` | Mantén `core` puro; eso va en adapters/config |
| Dejar que Hibernate cree la tabla | Escribe una migración `V*` de Flyway |
| Publicar el evento antes del commit | Escríbelo en `outbox` en la misma tx; el relay publica después |
| Controlador llamando a un repo JPA directo | Pasa por el puerto de salida + adapter |
