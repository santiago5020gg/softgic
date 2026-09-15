# ADR-0001: Arquitectura hexagonal (puertos y adaptadores) con core libre de framework
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

Los dos microservicios (`svc-solicitudes`, `svc-indicadores`) implementan lógica de negocio
sensible para un cliente de gobierno: una máquina de estados de solicitudes
(REGISTRADA→EN_ATENCION→RESUELTA→CERRADA, sin saltos inválidos) y reglas de agregación para
indicadores. Esta lógica debe ser fácil de probar de forma aislada (TDD, `core` sin
contenedor Spring), debe sobrevivir a cambios de infraestructura (cambiar de SQL Server a
otro motor, o de Kafka a otro broker, no debería tocar las reglas de negocio) y debe ser
auditable/evaluable como pieza central de la prueba técnica.

## Decisión

Se adopta arquitectura hexagonal (puertos y adaptadores) en ambos servicios:

- `core` (domain, dto, mapper, ports, usecase): Java puro, **sin** dependencias de Spring,
  JPA ni de `infrastructure`. Los casos de uso dependen solo de interfaces (puertos de
  entrada/salida). El dominio expone su propia máquina de estados
  (`Estado.puedeTransicionarA`) y lanza excepciones de dominio ante transiciones inválidas.
- `infrastructure/adapter/{in,out}`: adaptadores de entrada (controladores REST, listeners
  Kafka) y de salida (repositorios JPA, publicador de eventos), todos implementando los
  puertos definidos por el `core`.
- `application/config`: cableado de Spring (beans, `@Configuration`), fuera del `core`.
- Las dependencias apuntan siempre hacia adentro: `infrastructure` conoce al `core`, nunca
  al revés.

## Alternativas consideradas

- **Arquitectura en capas tradicional (controller-service-repository)**: más rápida de
  montar al inicio, pero acopla la lógica de negocio a JPA/Spring desde el día uno,
  dificultando el TDD del dominio y encareciendo cualquier cambio de infraestructura.
- **Clean Architecture con más capas (use case interactors + presenters explícitos)**:
  aporta poco valor adicional para el tamaño de este proyecto y añade ceremonia
  (boilerplate de mappers y capas) sin beneficio proporcional en una prueba técnica acotada.
- **Módulo único sin separación de puertos** (todo en `service` con `@Autowired` directo a
  repositorios JPA): descartado por ser exactamente lo que se busca evitar (acoplamiento
  del dominio a la infraestructura).

## Consecuencias

**Positivas**
- El `core` se prueba con JUnit puro, sin arrancar contexto Spring — tests rápidos y TDD
  real (Red→Green→Refactor).
- Cambiar un adaptador (p. ej. de SQL Server a otra BD, o el broker de eventos) no exige
  tocar el dominio ni los casos de uso.
- La máquina de estados y las reglas de negocio quedan explícitas y auditables en un solo
  lugar, sin mezclarse con preocupaciones transversales (transacciones, serialización JSON).

**Negativas / trade-offs**
- Más archivos y mappers explícitos (DTO ↔ dominio ↔ entidad JPA) que en un CRUD directo:
  mayor costo de boilerplate inicial.
- Curva de entrada más alta para quien no conozca el patrón: hay que entender el flujo
  puerto→adaptador antes de tocar código.
- Para una prueba técnica de alcance moderado, parte de esta separación es "sobre-ingeniería"
  si el criterio fuera solo velocidad de entrega; se acepta el costo porque la arquitectura
  hexagonal es explícitamente lo más evaluado en este reto.
