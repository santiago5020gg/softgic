---
name: frontend-component-standard
description: Use when writing, modifying, or reviewing React/TypeScript frontend code in this project — components, pages, hooks, api modules, UI primitives, styling, Redux slices, Zod schemas, Module Federation config, or frontend tests under frontend/shell or frontend/mf-remoto. Enforces the container/presentational split, reuse of the components/ui design system on MUI 7, responsive-by-default layout, and TDD.
---

# Frontend Component Standard (este proyecto)

## Overview

El frontend son **dos apps** bajo `frontend/`, integradas con **Module Federation sobre
Rspack**:

- **`shell`** (host) — React 19 + TypeScript, **MUI 7 + Emotion**, **Redux Toolkit**
  (sesión y/o negocio), **Zod** en las fronteras. Integra Keycloak (Authorization Code +
  PKCE; token en memoria, **nunca** en localStorage) y monta el remoto.
- **`mf-remoto`** — microfrontend federado; debe poder ejecutarse **standalone** además de
  federado.

Consumen el backend por REST sobre `/api/v1`. Se prueban con **Vitest + Testing Library**
(API mockeada con MSW). **Storybook** documenta al menos dos componentes reutilizables con
estados representativos.

## Capas — dónde va el código

| Concern | Carpeta |
|---|---|
| Capa HTTP tipada (único lugar con `fetch`/endpoints) | `api/` — `client.ts` (+ `ApiError`), módulos por recurso, `types.ts` |
| Hooks de datos (fetching/caché) | `hooks/` |
| Estado transversal (Redux) | `store/` — slices de sesión y negocio |
| Validación de fronteras | `schemas/` — esquemas Zod |
| **Primitivos presentacionales reutilizables (design system)** | `components/ui/` — Button, Input, Spinner, EmptyState, ErrorState, Pagination… sobre MUI |
| Componentes presentacionales por módulo | `components/<modulo>/` — solicitudes/, bandeja/, layout/ |
| Contenedores (smart) | `pages/<Page>/index.tsx` |
| Helpers puros (sin JSX) | `lib/` |

## Reglas del core

1. **Container vs presentational es obligatorio.**
   - Pages (`pages/*`) son contenedores: llaman hooks, tienen estado, manejan
     eventos/navegación y pasan props planas hacia abajo.
   - Components (`components/*`) son presentacionales: datos + callbacks solo por props.
     **Sin `fetch` ni llamadas a hooks de datos dentro de un componente presentacional.**
2. **Reusar antes de crear — por consistencia visual.** Construye la UI con los primitivos
   de `components/ui/*`. No hagas un `<button>`/input/estado vacío/error a mano; usa
   `Button`, `Input`, `EmptyState`, `ErrorState`, `Spinner`, `Pagination`. Si falta un
   primitivo genuinamente genérico, agrégalo a `components/ui/`.
3. **Estados explícitos obligatorios (requisito del reto):** carga, vacío, error y
   **autorización insuficiente**. Cada vista los maneja.
4. **Responsive por defecto + accesibilidad + teclado.** Mobile-first; layouts fluidos;
   controles reales con `aria-*`; navegable por teclado. No está listo hasta funcionar de
   móvil a escritorio.
5. **Todo acceso a API pasa por `api/*` y se consume vía `hooks/*`.** Tipos desde
   `api/types.ts`. Fallos → `ApiError` → `ErrorState`; vacío → `EmptyState`; carga →
   `Spinner`. Valida las respuestas/entradas relevantes con Zod.
6. **Seguridad en el cliente:** flujo OIDC con PKCE; sin contraseña ni client secret en el
   navegador; el token no se persiste en localStorage. Al recargar el detalle, la vista
   recupera sesión/estado y vuelve a consultar la fuente (→ escenario A6).

## Vistas mínimas (requisito del reto)

Login/redirección · bandeja con filtros · creación · detalle con línea de tiempo y acción
según rol · resumen analítico (indicadores por estado, categoría y tendencia diaria).

## Workflow (TDD + Gitflow)

- **TDD estricto Red → Green → Refactor.** Test primero (Vitest + Testing Library, API
  mockeada con MSW), co-locado como `*.test.tsx` junto al componente/hook.
- **Module Federation:** cambios en `rspack.config` (remotes/exposes) se verifican tanto
  federado (shell carga el remoto) como standalone (el remoto arranca solo).
- **Gitflow:** `feature/<desc>` desde `develop`, merge `--no-ff`, borra la rama. Conventional Commits.
- **Verifica antes de "listo" (por app):** `test`, `lint`, `typecheck`, `build`.

## Checklist de autorrevisión

- [ ] Fetching/estado en page/hook (contenedor); el componente es presentacional (solo props).
- [ ] Reusé `components/ui/*` en vez de duplicar estilos; primitivo genérico nuevo en `components/ui/`.
- [ ] Estados carga/vacío/error/no-autorizado cubiertos.
- [ ] Responsive de móvil a escritorio + accesible por teclado.
- [ ] Acceso vía `api/*` + `hooks/*`, con Zod en las fronteras relevantes.
- [ ] Un test falla primero y luego pasa (TDD real), con handlers/fixtures MSW actualizados.
- [ ] Si toqué federación: funciona federado **y** standalone.

## Errores comunes

| Error | Corrección |
|---|---|
| `fetch`/hook de datos dentro de un presentacional | Mueve los datos al page/hook; pasa props |
| `<button>`/estado a mano | Usa `components/ui/*` sobre MUI |
| Token en localStorage | Token en memoria; refresh vía flujo OIDC |
| Remoto que solo corre federado | Debe arrancar también standalone |
| Layout de ancho fijo / solo escritorio | Mobile-first + breakpoints + layout fluido |
