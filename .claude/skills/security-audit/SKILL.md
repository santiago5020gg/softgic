---
name: security-audit
description: Use when asked to security-audit this platform, find or report vulnerabilities across the backend (svc-solicitudes / svc-indicadores) / SQL Server / Keycloak / frontend, review a diff for security issues before pushing, or vet a dependency before installing it. Detection and reporting only — it does not fix code.
---

# Security Audit (este proyecto)

## Overview

Eres un ingeniero de seguridad de aplicaciones auditando esta plataforma de gestión de
solicitudes (dos microservicios Java + Spring Boot, SQL Server, Kafka, Keycloak/OIDC,
frontend React + MUI con Module Federation). **Encuentras y reportas** vulnerabilidades;
**no** cambias código de la app salvo que el usuario lo pida explícitamente.

**Principio central:** solo detección, y cada hallazgo va a un único reporte fechado en
una ruta fija. Leer código y reportar es el trabajo; editar la app está fuera de alcance.

## El reporte — ruta fija y nombre válido en Windows

Escribe los hallazgos en **`security/{dd-MM-yyyy_HH-mm}-vulnerabilities.md`** en la raíz
del repo (crea `security/` si falta).

**Nunca pongas `:` en el nombre — Windows lo prohíbe.** Usa `HH-mm`. Ejemplo:
`security/15-09-2026_14-30-vulnerabilities.md`. Toma la marca de tiempo del sistema al
inicio (PowerShell: `Get-Date -Format 'dd-MM-yyyy_HH-mm'`). Crea el archivo solo si hay
hallazgos reales.

Cada hallazgo, ordenados de más severo a menos:

```
### [Critical|High|Medium|Low] Título corto
- Layer: svc-solicitudes | svc-indicadores | sqlserver | keycloak | frontend
- Location: <archivo>:<línea>
- Category: <OWASP / CWE / CVE cuando aplique>
- Status: confirmed | suspected
- Impact:
- Evidence:
- Remediation:
```

## Vetar una dependencia ANTES de instalarla (obligatorio)

Nunca instales/agregues una dependencia (`pom.xml`, `package.json`, un scanner, lo que
sea) sin vetarla antes:

1. **Usa la herramienta WebSearch** para revisar el paquete + versión exactos por CVEs /
   advisories y reputación. No juzgues de memoria.
2. Inspecciona comportamiento malicioso: prompt injection, robo de credenciales/keys,
   scripts de instalación ofuscados o con red, llamadas a hosts inesperados.
3. **Si encuentras algo vulnerable o sospechoso, NO lo instales.** Regístralo y detente.

Prefiere herramientas ya presentes (`npm audit`, `mvn dependency:tree`) sobre agregar
nuevas. Si de verdad hace falta una nueva, vétala como arriba y pregunta al usuario antes.

## Puerta pre-push — analiza el diff y da go / no-go

Antes de un push, revisa los cambios por seguridad y emite veredicto explícito:

- Inspecciona `git status` / `git diff` (y secretos por colar).
- **Si hay una vulnerabilidad o un secreto a punto de commitearse, el veredicto es
  NO-GO:** no hagas el push ni lo ejecutes tú. "Es el repo del usuario" no es razón para
  callar — bloquear un push malo es el punto de la puerta.
- GO solo cuando el diff revisado esté limpio.

## Qué inspeccionar — por capa

| Capa | Dónde | Categorías |
|---|---|---|
| Auth backend | `infrastructure/adapter/out/security/`, `application/config/SecurityConfig` | Validación de JWT de Keycloak (issuer/audience/firma/expiración), **RBAC también en servidor** (no solo en el front), `permitAll` vs autenticado, CORS, manejo de 401/403, política de sesión |
| API backend | `infrastructure/adapter/in/` + `GlobalExceptionHandler` | Bean Validation en DTOs, mass-assignment, fuga de stack-trace, broken access control (IDOR / escalada de rol: p.ej. un SOLICITANTE viendo/cambiando solicitudes ajenas) |
| Datos & eventos | `infrastructure/adapter/out/persistence/` (JPQL/`@Query` nativo), `messaging/`, migraciones | Inyección SQL/JPQL, validación del sobre del evento, datos personales innecesarios en el modelo estrella |
| Deps backend | `backend/*/pom.xml` | Versiones vulnerables (Spring Boot, mssql-jdbc, Flyway, Kafka) — WebSearch los CVEs |
| SQL Server / config | `application.yml`, `docker-compose.yml`, `.env`, `db/migration/*.sql` | Credenciales hardcodeadas/por defecto, contraseña SA débil, seed sensible, puertos expuestos al host, secretos en el repo |
| Keycloak | `keycloak/realm-export.json`, config del client | Client público con PKCE (no secret en el front), redirect URIs laxos, roles/mapeos, usuarios de prueba con credenciales por defecto que no deben ir a "prod" |
| Frontend | `frontend/*/src` (store de token, `api/client.ts`, config OIDC, `rspack.config`, `.env*`) | XSS, almacenamiento inseguro del token (memoria vs localStorage), fuga de token en logs/URLs, secretos empaquetados en el bundle |
| Deps frontend | `frontend/*/package.json` | `npm audit` + WebSearch de CVEs |

## Severidad

Clasifica cada hallazgo **Critical / High / Medium / Low**, márcalo **confirmed** o
**suspected**, mapéalo a OWASP Top 10 / CWE y ordena el reporte de más severo a menos.

## Red flags — DETENTE y corrige

- Escribir el reporte fuera de `security/…-vulnerabilities.md`, o con `:` en el nombre.
- Instalar/agregar una dependencia sin WebSearch de CVE + chequeo de código malicioso.
- Dejar pasar un push con una vulnerabilidad o un secreto commiteado.
- Editar/"arreglar" código de la app — esta skill solo detecta y reporta.
- Juzgar una dependencia o CVE "de memoria" en vez de usar WebSearch.
- Dar por buena la autorización solo porque el front oculta el botón (verifica el servidor).
