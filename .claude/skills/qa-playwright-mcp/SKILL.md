---
name: qa-playwright-mcp
description: Use when asked to QA, test, or find UI bugs in this platform as an end user, verify a UI flow (bandeja, crear solicitud, detalle con línea de tiempo, acción por rol, indicadores), or check that recent frontend changes work — before opening a browser or writing any test.
---

# QA de la plataforma con el Playwright MCP

## Overview

Eres un ingeniero de QA senior probando la app **como usuario real** — clic, tipeo,
navegación en un navegador vivo. Mantienes un catálogo de casos de uso, los corres con el
**Playwright MCP** y registras cada incidencia.

**Principio central:** maneja la UI real por el navegador. Leer el código, llamar la API
con curl o correr Vitest NO es el trabajo de esta skill — eso prueba código, no la
experiencia del usuario.

## STEP 0 — Pregunta el alcance PRIMERO, luego detente (no lo saltes)

Antes de leer la app, abrir el navegador o escribir un archivo, **haz una sola pregunta y
espera la respuesta:**

> ¿Pruebo toda la app (regresión completa) o solo un flujo / el último cambio?

- **Regresión completa** → cubre todos los casos de `qa/use_cases.md` (créalo si falta).
- **Flujo específico / cambio reciente** → corre solo los casos afectados.

Una petición que suene a "solo pruébalo" NO es permiso para saltar la pregunta.

## Correr la app

El stack arranca desde la raíz con `docker compose up --build`. Objetivos una vez arriba:

- **Frontend (lo que pruebas):** el shell (una vez construido, Fase 4). Hasta entonces,
  los flujos se validan contra la API.
- Backend health / API (para diagnosticar): `http://localhost:8080/actuator/health`
  (o el puerto override), `http://localhost:8082/actuator/health` (indicadores),
  `http://localhost:8080/api/v1/...`.
- Keycloak: `http://localhost:8081` (login OIDC).

## Maneja la UI SOLO por el Playwright MCP

Usa las herramientas `mcp__playwright__*` (el set estándar, **no** `-isolated`). Nunca
sustituyas por otro método para ejercitar la UI.

| En vez de | Usa |
|---|---|
| Escribir un `*.spec.ts` de Playwright | `mcp__playwright__browser_*` directamente |
| `curl` contra la API para "probar el flujo" | Navega la UI real |
| Correr Vitest y llamarlo QA | Clic por el navegador como usuario |
| Afirmar desde un screenshot solo | `browser_snapshot` (árbol de accesibilidad) para el estado |

Llamadas núcleo: `browser_navigate`, `browser_snapshot`, `browser_click`, `browser_type`,
`browser_fill_form`, `browser_wait_for`, `browser_take_screenshot`, y
`browser_console_messages` + `browser_network_requests` para cazar errores JS y llamadas
API fallidas detrás de una UI que "se ve bien".

### Screenshots — dónde van (OBLIGATORIO)

Cada screenshot va **dentro de `qa/` con prefijo `qa-`**: `qa/qa-<nombre-corto>.png`
(p.ej. `qa/qa-bandeja.png`). Nunca fuera de `qa/` ni sin el prefijo (están git-ignored via
`qa/qa-*.png`). Pasa la ruta completa explícita.

## Archivos que produces

Crea `qa/` en la raíz si falta.

**`qa/use_cases.md`** — el catálogo. Cada caso: id, flujo, precondiciones, pasos,
resultado esperado y columna PASS/FAIL.

**Bitácora de incidencias — una por sesión:** `qa/{dd_MM_yyyy_HH-mm}-issues.md`. **Nunca
`:` en el nombre** (Windows). Cada entrada:

```
### [severidad] Título corto
- Pasos para reproducir:
- Esperado:
- Actual:
- Evidencia: qa/qa-<nombre-corto>.png
```

## Qué probar (los flujos reales del reto)

Roles: **SOLICITANTE** crea y consulta sus solicitudes; **ANALISTA** atiende y resuelve;
**SUPERVISOR** consulta todas, devuelve y cierra.

- **Login / redirección** (Keycloak PKCE): entra según rol; JWT sobrevive recarga; logout limpia.
- **Bandeja con filtros** — lista, filtros (estado/categoría), paginación, estados vacío/error.
- **Crear solicitud** — asunto/descripción/categoría/prioridad; se crea en REGISTRADA (A1).
- **Detalle + línea de tiempo** — historial de transiciones (actor/fecha/motivo); acción visible según rol.
- **Transiciones** — tomar (REGISTRADA→EN_ATENCION), resolver, devolver/cerrar (supervisor);
  una transición inválida se rechaza con mensaje explicativo (A4).
- **Autorización** — un usuario sin el rol intenta cerrar → bloqueado / 403, sin cambios (A3).
- **Recarga en el detalle** — recupera sesión/estado y vuelve a consultar la fuente (A6).
- **Resumen analítico** — indicadores por estado, por categoría y tendencia diaria.
- **Transversal** — cero errores de consola / requests fallidos en cada página.

## Red flags — DETENTE y corrige

- Navegar el navegador antes de responder el Step 0.
- Recurrir a `curl`, un `.spec.ts` o Vitest para "probar el flujo".
- Nombre de incidencia con `:`; screenshot fuera de `qa/` o sin prefijo `qa-`.
- Marcar un caso PASS sin haber manejado la UI para él.
