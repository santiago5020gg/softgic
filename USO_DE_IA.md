# Uso de IA

Documento requerido por el reto (§6.8): herramientas utilizadas, actividades apoyadas,
verificaciones realizadas y decisiones propias.

## Herramientas utilizadas

- **Claude Code** (asistente de IA) para andamiaje del proyecto, redacción de
  configuración, migraciones y documentación, y como apoyo de revisión.

## Actividades apoyadas por IA

- Diseño del blueprint de arquitectura (hexagonal, 2 microservicios, eventos, microfrontends).
- Andamiaje y desarrollo por fases: `docker-compose`, Dockerfiles multi-stage, servicios
  backend hexagonales, migraciones Flyway (SQL Server), relay Outbox + consumidor Kafka,
  seguridad Keycloak, frontend shell + microfrontend (Module Federation), suite Karate,
  pipeline GitLab CI, Helm y documentación.
- El desarrollo se orquestó con subagentes especializados (backend, frontend, seguridad,
  QA), cada fase **verificada end-to-end** por el autor antes de integrarse.

## Verificaciones realizadas

- [x] `docker compose up --build` levanta el stack y ambos servicios reportan `UP`.
- [x] Las migraciones Flyway se aplican sin error en SQL Server (operacional + estrella).
- [x] Pruebas unitarias de dominio/casos de uso verdes (JUnit 5 + Mockito); aggregate
      `Solicitud` ~99% de cobertura (JaCoCo).
- [x] Flujo de eventos end-to-end (Outbox → Kafka → indicadores) e **idempotencia** (A5).
- [x] Seguridad: 401 sin token, 403 por rol (A3), 200 con rol correcto — tokens reales.
- [x] Frontend verificado en navegador real (Playwright): login Keycloak PKCE → bandeja
      con datos del backend → panel **federado** de indicadores; 0 errores de consola.
- [x] Suite **Karate**: 4/4 escenarios verdes (A1, A3, recorrido, 401).

### Bugs detectados y corregidos por el autor durante la revisión

- `@OneToMany` unidireccional insertaba la FK en NULL en SQL Server → relación bidireccional.
- `svc-indicadores` sin CORS → el panel federado fallaba con *Failed to fetch* → CORS añadido.

## Decisiones propias (no delegadas a la IA)

- **Broker: Kafka** (frente a RabbitMQ) por afinidad con el enfoque event-driven/analítico.
- **Persistencia: SQL Server** con Flyway como dueño del esquema.
- **Alcance por fases**, priorizando un camino feliz demostrable y ejecutable sobre
  amplitud incompleta (acorde a la rúbrica del reto).

> Todo el contenido generado con IA fue revisado y es comprendido por el autor, que
> puede explicarlo y modificarlo (condición §8 del reto).
