# ADR-0002: Dos microservicios con CQRS — escritura (Solicitudes) separada de lectura analítica (Indicadores)
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

La plataforma tiene dos necesidades con perfiles muy distintos: (a) registrar y gestionar
solicitudes operacionales con fuerte consistencia transaccional y una máquina de estados
estricta, y (b) exponer indicadores/reportes agregados para consulta analítica, que se
consultan con más frecuencia de lectura, distinta cardinalidad de filtros y un modelo de
datos orientado a agregación (no a normalización transaccional). Modelar ambas
responsabilidades en un único servicio y un único esquema obligaría a elegir entre un
modelo normalizado (bueno para escritura, malo para agregados) o un modelo desnormalizado
(bueno para reportes, malo para integridad transaccional).

## Decisión

Se separan en dos microservicios independientes, cada uno con su propia base de datos:

- **svc-solicitudes** (`com.gov.solicitudes`): dueño del ciclo de vida de la solicitud
  (comandos y consultas operacionales). Modelo relacional normalizado. Es la fuente de
  verdad transaccional.
- **svc-indicadores** (`com.gov.indicadores`): modelo de lectura (CQRS), alimentado de forma
  asíncrona por los eventos de dominio que publica svc-solicitudes (vía Outbox + Kafka).
  Mantiene un esquema en estrella optimizado para consultas agregadas.

La comunicación entre ambos es unidireccional y asíncrona (eventos), no hay llamadas
síncronas de indicadores hacia solicitudes para servir sus consultas.

## Alternativas consideradas

- **Monolito modular** con un solo servicio y dos módulos internos: más simple de desplegar
  en esta prueba técnica, pero no permite demostrar el patrón CQRS ni el desacoplamiento por
  eventos que el reto pide evaluar, y mezclaría el modelo transaccional con el analítico en
  el mismo esquema/tx.
- **Un solo servicio con dos esquemas y consultas federadas en tiempo real** (JOIN entre
  bases): descartado porque acopla fuertemente ambos dominios a nivel de infraestructura de
  datos y reintroduce el problema de un modelo único para ambos casos de uso.
- **CQRS dentro del mismo servicio** (mismo proceso, distinto datasource de lectura):
  reduce la complejidad operativa (un despliegue) pero no demuestra microservicios
  independientes ni el ciclo de vida de eventos entre servicios desacoplados, que es parte
  del alcance evaluado.

## Consecuencias

**Positivas**
- Cada servicio evoluciona su modelo de datos según su propio caso de uso (normalizado vs.
  estrella) sin comprometer al otro.
- Indicadores puede escalar horizontalmente o cambiar de motor de lectura sin afectar la
  disponibilidad transaccional de Solicitudes.
- Los dos servicios se pueden desplegar, versionar y probar de forma independiente.

**Negativas / trade-offs**
- Consistencia eventual: los indicadores reflejan el estado de las solicitudes con un
  retraso (el tiempo entre commit de la transacción de negocio y el consumo del evento).
  No es apto para pantallas que requieran lectura fuertemente consistente del agregado.
- Mayor complejidad operativa: dos bases de datos, dos pipelines de despliegue, y la
  necesidad de un mecanismo confiable de propagación de eventos (ver ADR-0003).
- Depuración más difícil: un defecto en los indicadores puede originarse en el evento
  publicado, en el consumidor, o en un desfase de esquema entre ambos modelos.
