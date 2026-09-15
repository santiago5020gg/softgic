# ADR-0006: Seguridad con Keycloak — Authorization Code + PKCE en el front, Resource Server + RBAC en el back
- Estado: Aceptada
- Fecha: 2026-09-15

## Contexto

Al ser una plataforma para un cliente de gobierno, la autenticación y autorización deben
seguir estándares abiertos y auditables (no un esquema de sesiones/roles ad-hoc), soportar
un frontend público (SPA servida al navegador, sin backend propio que guarde secretos de
cliente) y permitir que cada microservicio backend valide y autorice peticiones de forma
independiente, sin depender de una sesión centralizada por servidor.

## Decisión

- **Keycloak** (`quay.io/keycloak/keycloak:25.0`) actúa como Identity Provider (IdP) OIDC,
  desplegado en `docker-compose.yml` (modo `start-dev` para el entorno local de la prueba).
- El **frontend** (shell) implementa el flujo **OIDC Authorization Code con PKCE**: es un
  cliente público (sin client secret embebido en el navegador), y el `code_verifier`/
  `code_challenge` de PKCE mitiga la intercepción del código de autorización.
- Cada **microservicio backend** actúa como **Resource Server OAuth2**: valida el JWT
  (access token) emitido por Keycloak en cada petición (firma, expiración, issuer), sin
  mantener estado de sesión propio.
- La **autorización** se resuelve por **RBAC** en el backend a partir de los roles/claims
  del token (p. ej. `@PreAuthorize` sobre casos de uso o endpoints), nunca confiando en
  lógica de autorización solo del lado del cliente.

## Alternativas consideradas

- **Implicit Flow** (sin PKCE, token directamente en el fragmento de la URL): es el flujo
  legado para SPAs; se descarta porque expone el access token en el historial del navegador
  y no tiene protección equivalente a PKCE contra interceptación del código; OAuth 2.0
  Security Best Current Practice lo desaconseja explícitamente para clientes públicos.
- **Authorization Code con client secret en el frontend**: inviable — un secreto embebido en
  código servido al navegador no es un secreto; cualquier cliente público debe usar PKCE en
  lugar de (o además de) un secret.
- **Sesiones de servidor + cookies (autenticación tradicional, sin OIDC)**: simplifica el
  frontend, pero acopla la autenticación a un backend con estado y no interopera con un
  ecosistema de microservicios donde cada servicio debe poder validar el token de forma
  independiente y sin estado (stateless Resource Server).
- **Autorización solo en el frontend** (ocultar botones/rutas según rol, sin validar en el
  backend): se descarta de plano — es un antipatrón de seguridad; el RBAC debe imponerse en
  el servidor, la ocultación en el front es solo UX, nunca control de acceso real.

## Consecuencias

**Positivas**
- Estándar abierto (OIDC/OAuth2) auditable, con soporte nativo en Spring Security
  (Resource Server) y en librerías de frontend maduras para PKCE.
- Los backends permanecen sin estado respecto a la autenticación: escalan horizontalmente
  sin sesiones pegajosas ni almacenamiento de sesión compartido.
- El RBAC en el servidor es el punto único de verdad de autorización, verificable e
  independiente de lo que muestre la UI.

**Negativas / trade-offs**
- Infraestructura adicional a operar (Keycloak) y a configurar (realm, clientes, roles,
  mappers de claims) — para el alcance de la prueba técnica esto se ejecuta en `start-dev`,
  no en modo productivo (sin HA, sin hardening completo).
- Cada backend debe mantener su propia configuración de validación de JWT y de mapeo de
  roles/claims a permisos; un desalineamiento entre lo emitido por Keycloak y lo esperado
  por RBAC en cada servicio es una fuente típica de bugs de autorización si no se prueba
  explícitamente (caso A3 del reto).
- El flujo PKCE añade complejidad al cliente frontend (generación y verificación de
  `code_verifier`/`code_challenge`, manejo de redirecciones) frente a un login más simple,
  pero es el costo necesario para una SPA pública y segura.
