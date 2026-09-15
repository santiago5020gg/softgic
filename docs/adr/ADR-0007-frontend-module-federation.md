# ADR-0007: Frontend — shell + microfrontend con Module Federation sobre Rspack; MUI 7, Redux Toolkit, Zod
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

El frontend debe reflejar, del lado cliente, la misma filosofía de desacoplamiento que los
microservicios backend: distintos equipos/dominios (p. ej. gestión de solicitudes vs.
visualización de indicadores) deberían poder desarrollar y desplegar su parte de la UI de
forma independiente, sin forzar un único build monolítico de frontend. Además se necesita
un stack moderno (React 19), tipado y validación de datos que lleguen del backend/formularios
(Zod), un sistema de componentes consistente con el estándar visual esperado en aplicaciones
de gobierno (MUI 7), y manejo de estado predecible para flujos de solicitudes con múltiples
pantallas (Redux Toolkit).

## Decisión

- El frontend se estructura como **shell (host) + microfrontend remoto (`mf-remoto`)**
  usando **Module Federation** sobre **Rspack** como bundler.
  - El *shell* aloja el layout global, la autenticación (OIDC/PKCE, ADR-0006) y el
    enrutamiento de alto nivel.
  - El *microfrontend remoto* expone las pantallas de un dominio (p. ej. solicitudes o
    indicadores) como módulos federados, cargados en runtime por el shell.
- **React 19** como librería de UI, **MUI 7** (+ Emotion) como sistema de componentes,
  **Redux Toolkit** para estado compartido/complejo, y **Zod** para validación de esquemas
  (formularios y respuestas de API) en el borde de la aplicación.
- Patrón **container/presentational** para separar lógica de datos/estado de la
  presentación pura de componentes.

## Alternativas consideradas

- **Webpack Module Federation** (en lugar de Rspack): es la implementación original y más
  documentada de Module Federation, pero Rspack (compatible con la misma API de Module
  Federation) ofrece tiempos de build sensiblemente menores al estar escrito en Rust; se
  prioriza velocidad de desarrollo/CI para el alcance de esta prueba técnica.
- **Monolito de frontend único (sin Module Federation)**: más simple de configurar y
  depurar, pero no demuestra el desacoplamiento por dominio que el reto pide evaluar (A6) y
  obligaría a un único pipeline de build/deploy para todo el frontend.
- **Micro-frontends vía iframes o Web Components independientes** (sin Module Federation):
  aísla completamente cada microfrontend (incluso en runtime de JS), pero complica compartir
  estado, tema de MUI y sesión entre shell y remoto; Module Federation permite compartir
  dependencias (React, MUI) en runtime evitando duplicarlas por cada remoto.
- **Context API + hooks en lugar de Redux Toolkit**: suficiente para estado local simple,
  pero para flujos con estado compartido entre pantallas/microfrontend (filtros, sesión,
  datos de solicitudes en curso) Redux Toolkit da una estructura más predecible y
  depurable (DevTools, slices) entre el shell y el remoto.
- **Validación manual o solo en backend (sin Zod en el front)**: retrasa el feedback al
  usuario hasta el round-trip al servidor y duplica reglas de validación sin un esquema
  compartido/tipado; Zod permite validar en el borde del cliente con el mismo esquema que
  tipa los datos en TypeScript.

## Consecuencias

**Positivas**
- El shell y el microfrontend remoto pueden evolucionar, compilarse y (en un escenario real)
  desplegarse de forma independiente, reflejando la separación de dominios del backend.
- Module Federation comparte en runtime dependencias pesadas (React, MUI) entre shell y
  remoto, evitando duplicarlas en el bundle final.
- Zod da validación de datos consistente con el tipado TypeScript en un único esquema,
  reduciendo bugs de forma/tipo entre frontend y backend.

**Negativas / trade-offs**
- Module Federation añade complejidad de configuración (exposición/consumo de remotos,
  versiones compartidas de dependencias) frente a un bundle único; los errores de
  incompatibilidad de versiones entre shell y remoto pueden ser difíciles de diagnosticar.
- Rspack, al ser más reciente que Webpack, tiene un ecosistema de plugins y documentación
  algo menor; para esta prueba técnica se acepta ese riesgo por la ganancia en velocidad de
  build.
- Redux Toolkit introduce boilerplate de slices/store frente a soluciones más ligeras de
  estado; se justifica por la necesidad de estado compartido y predecible entre shell y
  microfrontend remoto, no para estado puramente local de un componente.
