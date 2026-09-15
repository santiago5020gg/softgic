# Uso de IA

Documento requerido por el reto (§6.8): herramientas utilizadas, actividades apoyadas,
verificaciones realizadas y decisiones propias.

## Herramientas utilizadas

- **Claude Code** (asistente de IA) para andamiaje del proyecto, redacción de
  configuración, migraciones y documentación, y como apoyo de revisión.

## Actividades apoyadas por IA

- Diseño del blueprint de arquitectura (hexagonal, 2 microservicios, eventos, microfrontends).
- Generación del andamiaje inicial: `docker-compose`, Dockerfiles multi-stage,
  `pom.xml`, `application.yml`, migraciones Flyway (SQL Server) y enums de dominio.
- Redacción de README y de este documento.

## Verificaciones realizadas (a completar durante el desarrollo)

- [ ] `docker compose up --build` levanta el stack y ambos servicios reportan `UP`.
- [ ] Las migraciones Flyway se aplican sin error en SQL Server.
- [ ] Las pruebas unitarias de dominio pasan (`EstadoTest`).
- [ ] (Fases siguientes) Karate A1/A3/recorrido, cobertura JaCoCo, auditoría de seguridad.

## Decisiones propias (no delegadas a la IA)

- **Broker: Kafka** (frente a RabbitMQ) por afinidad con el enfoque event-driven/analítico.
- **Persistencia: SQL Server** con Flyway como dueño del esquema.
- **Alcance por fases**, priorizando un camino feliz demostrable y ejecutable sobre
  amplitud incompleta (acorde a la rúbrica del reto).

> Todo el contenido generado con IA fue revisado y es comprendido por el autor, que
> puede explicarlo y modificarlo (condición §8 del reto).
