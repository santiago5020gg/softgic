# ADR-0005: SQL Server con Flyway como dueño del esquema; modelo operacional normalizado + modelo analítico en estrella
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

El cliente es una entidad de gobierno; SQL Server es un motor común en ese entorno (y es el
exigido/asumido por el reto). Se necesita un mecanismo de control de versiones del esquema
de base de datos que sea explícito, auditable y reproducible en cualquier entorno
(`docker compose up --build` debe dejar el esquema correcto sin intervención manual), y que
evite que Hibernate genere o modifique el esquema de forma implícita en un sistema que debe
ser predecible y auditable.

## Decisión

- **SQL Server** (`mcr.microsoft.com/mssql/server:2022-latest`) es el motor de persistencia
  para ambos servicios, con dos bases de datos separadas (`solicitudes`, `indicadores`)
  creadas por el contenedor de inicialización (`mssql-init`) antes de que arranquen los
  servicios (`service_completed_successfully`).
- **Flyway es el único dueño del esquema**: toda evolución de estructura de datos es una
  migración versionada `V*__*.sql` (T-SQL) en `src/main/resources/db/migration` de cada
  servicio. Hibernate/JPA se configura en `ddl-auto: validate`: valida que las entidades
  coincidan con el esquema, pero nunca lo crea ni lo modifica.
- **svc-solicitudes** usa un modelo relacional **normalizado** (entidades operacionales:
  solicitud, estado, historial de transición, etc.), apto para integridad transaccional y
  la máquina de estados del dominio.
- **svc-indicadores** usa un **esquema en estrella** (tablas de hechos + dimensiones),
  optimizado para consultas agregadas de indicadores, alimentado por los eventos consumidos
  desde svc-solicitudes.

## Alternativas consideradas

- **`ddl-auto: update` (Hibernate genera/actualiza el esquema)**: más rápido para prototipar,
  pero impredecible en producción (Hibernate puede inferir tipos o índices distintos a los
  intencionados) y no auditable — no hay un historial versionado de qué cambió y por qué.
  Inaceptable para un cliente de gobierno donde el esquema debe ser trazable.
- **PostgreSQL o MySQL** en lugar de SQL Server: viables técnicamente y con mejor
  tooling open-source, pero SQL Server es el motor consistente con el contexto real del
  cliente (gobierno, stack Microsoft) que este reto asume.
- **Un solo esquema compartido normalizado también para indicadores** (sin modelo en
  estrella): simplificaría el modelo de datos, pero las consultas agregadas sobre un modelo
  normalizado con muchos JOINs degradan el rendimiento de lectura analítica a medida que
  crece el volumen; el esquema en estrella está diseñado específicamente para ese patrón de
  acceso.
- **Liquibase en lugar de Flyway**: alternativa equivalente en propósito; se elige Flyway
  por su modelo más simple (SQL plano versionado, sin XML/YAML de changesets) y su uso más
  extendido en proyectos Spring Boot.

## Consecuencias

**Positivas**
- El esquema de cada base de datos es 100% reproducible y auditable: cualquier entorno
  nuevo (`docker compose up --build`) queda con exactamente el mismo esquema, sin pasos
  manuales.
- `ddl-auto: validate` detecta en el arranque cualquier desalineación entre entidades JPA y
  esquema real, evitando sorpresas en tiempo de ejecución.
- Cada modelo de datos está optimizado para su patrón de acceso real (transaccional vs.
  analítico), en línea con la separación CQRS de ADR-0002.

**Negativas / trade-offs**
- Toda evolución de esquema exige escribir y versionar una migración explícita — más
  fricción que dejar que Hibernate la infiera, especialmente durante el desarrollo temprano.
- T-SQL específico de SQL Server: las migraciones no son portables a otro motor sin
  reescritura.
- Mantener dos modelos de datos (normalizado + estrella) para el mismo dominio implica
  duplicación conceptual y un mapeo explícito entre el evento de dominio y las dimensiones/
  hechos de indicadores, que debe mantenerse sincronizado a mano en cada nuevo tipo de
  evento.
