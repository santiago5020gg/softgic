---
name: validate-before-push
description: Use when about to push, merge-and-push, or otherwise ship code to ANY git branch in this project — i.e. immediately before running git push — including quick one-line fixes, changes whose tests "already passed", and pushes made under time pressure. Not for validation at any other time.
---

# Validate Before Push (este proyecto)

## Overview

Eres la puerta de entrega de esta plataforma de solicitudes. **Nada llega a una rama
remota hasta que cada validador que aplique al diff pase** — estándares de backend, de
frontend, seguridad y QA, seleccionados por lo que el diff realmente toca (ver reglas de
salto). No ejecutas el push mientras algo esté en rojo: arreglas y repites hasta verde.

**Principio central:** correr Gitflow bien y tener tests en verde NO es lo mismo que
validar. Esta puerta corre las skills del proyecto como subagentes.

**Alcance — solo pre-push.** Corre esta puerta solo justo antes de un `git push`. No es un
paso de CI, ni un chequeo por commit, ni una revisión general.

## Los cuatro validadores

| Skill | Valida | Correr cuando |
|---|---|---|
| `backend-hexagonal-standard` | Estándares Java/Spring bajo `backend/` | el diff toca `backend/` |
| `frontend-component-standard` | Estándares React/TS bajo `frontend/` | el diff toca `frontend/` |
| `security-audit` | Vulnerabilidades en backend / SQL Server / Keycloak / frontend | **siempre** |
| `qa-playwright-mcp` | Comportamiento E2E / UI como usuario real | el diff contiene **código** |

**Qué cuenta como cambio de código:** cualquier archivo bajo `backend/` o `frontend/`, o
cualquier `.sql` (migraciones Flyway). Todo lo demás — Markdown/docs, `.claude/` skills &
hooks, `.gitignore`, `docker-compose.yml`, `helm/`, `.gitlab-ci.yml`, imágenes — es
**cambio no-código**.

Dos reglas de salto, ambas probadas con `git diff`:

- **Salta una skill de estándar de código** (`backend-…` / `frontend-…`) cuando *su* capa
  tiene cero cambios en el diff.
- **Push solo de docs/config** — cuando el diff no tiene ningún cambio de código: corre
  **solo `security-audit`** y **salta QA**.

`security-audit` corre **siempre**. QA corre solo cuando el diff contiene código.

## La puerta — orden obligatorio

0. **Clasifica el diff primero** (`git diff`): qué capas cambiaron y qué validadores
   aplican. Si es docs/config-only, el set aplicable es **`security-audit` solo**.
1. **En paralelo**, despacha un subagente de fondo por cada validador aplicable de
   `backend-hexagonal-standard`, `frontend-component-standard` y `security-audit`. Son
   independientes — concurrentes, no en serie.
2. **Recoge los reportes.** Si alguno falla, **arréglalo tú**, y re-corre *solo* el
   validador afectado. Repite hasta que todos estén verdes.
3. **(Solo si hay código.) Con 1–2 en verde**, despacha `qa-playwright-mcp`. Probar un
   build que aún viola estándares o trae una vuln desperdicia la corrida.
4. **Si QA reporta algo, arréglalo y reinicia desde el paso 1** — un fix de código puede
   re-romper un estándar o introducir una vuln.
5. **Veredicto.**
   - **Todos los aplicables en verde → GO.** El push puede proceder.
   - **Algo en rojo → NO-GO:** no hagas el push ni lo ejecutes tú. Di exactamente qué falla.

El estándar de cada skill es la autoridad de qué significa "pasar". No relajes criterios,
no muestrees en vez de cubrir, no declares verde sin su reporte.

## (Opcional) Enforcement por hook

El proyecto de referencia usaba un hook `PreToolUse` que bloquea `git push` salvo que
exista un marcador fresco `.claude/.push-approved` (creado solo en un GO real, con la
herramienta Write, no por shell). Aquí el hook aún **no está cableado**; si el usuario lo
quiere, se añade en `.claude/settings.json` + `.claude/hooks/`. Hasta entonces, esta
puerta se corre por disciplina antes de cada push.

## Red flags — DETENTE, estás por saltar la puerta

- `git push` sin haber corrido cada validador que aplica al diff.
- Saltar `security-audit` — corre en **todos** los push.
- Saltar QA cuando el diff sí tiene código (`backend/`, `frontend/`, `*.sql`).
- "Es un cambio de una línea / trivial." / "Los tests ya pasaron antes."
- "Vamos tarde, saltemos la verificación." / "Push ahora y valido después."
- Ejecutar `git push` con cualquier validador en rojo.

## Racionalizaciones comunes

| Racionalización | Realidad |
|---|---|
| "Fix de una línea, salto la puerta" | Un typo en un label puede shippear XSS o romper un flujo. Tamaño ≠ seguridad. |
| "Los tests ya pasaron" | Tests unitarios no son auditoría de seguridad, ni revisión de estándares, ni QA E2E. |
| "Vamos tarde" | La presión de tiempo es justo cuando se cuelan regresiones. |
| "Gitflow + tests = listo" | Gitflow controla *cómo* mergea el código, no *si* es correcto/seguro. |
| "Solo cambió el front, corro todo / nada" | Salta una skill de estándar solo si su capa no cambió. `security-audit` siempre; QA si hay código. |
