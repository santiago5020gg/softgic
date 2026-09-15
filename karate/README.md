# Suite Karate (E2E de API)

Pruebas de aceptación ejecutables sobre la API real, requisito §5 del reto. Cubren:

- **A1** — un SOLICITANTE registra una solicitud válida → `201`, estado `REGISTRADA`, con código e historial.
- **Recorrido** — ANALISTA toma (`→ EN_ATENCION`) y resuelve (`→ RESUELTA`).
- **A3** — un SOLICITANTE intenta cerrar → `403` y la solicitud no cambia; sin token → `401`.
- Catálogo de categorías devuelve solo activas.

## Estrategia

Se ejecuta **contra el stack local** (levantado con `docker compose up --build`), dentro
de la red del compose, para que los tests resuelvan los hostnames internos y obtengan
tokens reales de Keycloak. El realm fija `frontendUrl = http://localhost:8081`, así el
`iss` del token coincide con el que valida el backend aunque el token se pida por la red
interna (`keycloak:8080`).

`karate-config.js` obtiene un token (password grant, client `solicitudes-shell`) para
cada rol y los expone como `tokenSolicitante` / `tokenAnalista` / `tokenSupervisor`.

## Ejecución

Con el stack corriendo:

```bash
docker run --rm --network prueba-softgic_ps-net \
  -v "$PWD/karate:/app" -w /app maven:3.9-eclipse-temurin-21 mvn -B test
```

Variables opcionales: `SOLICITUDES_URL` (default `http://svc-solicitudes:8080/api/v1`),
`TOKEN_URL` (default `http://keycloak:8080/realms/solicitudes/protocol/openid-connect/token`).

Reporte HTML: `target/karate-reports/karate-summary.html`.

## Resultado

`scenarios: 4 | passed: 4 | failed: 0 — BUILD SUCCESS` (Karate 1.4.1).
