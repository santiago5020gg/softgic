# Helm — plataforma-solicitudes

Chart de los dos microservicios (`svc-solicitudes`, `svc-indicadores`). La
infraestructura (SQL Server, Kafka, Keycloak) **no** se despliega aquí: se referencia
por configuración (`values.yaml`), acorde al reto (no se exige clúster público).

## Contenido

- `Deployment` por servicio: imagen, `replicaCount`, `envFrom` ConfigMap (config no
  sensible) + `env` con la contraseña de BD desde un `Secret` (referencia), probes
  `readiness`/`liveness` sobre `/actuator/health/*`, y `resources` (requests/limits).
- `Service` ClusterIP (puerto 8080) por servicio.
- `ConfigMap` por servicio con la config no sensible (datasource URL/usuario, Kafka,
  Keycloak issuer/JWKS).
- `Secret` de plantilla (solo si no se usa `existingSecret`).

## Instalación

```bash
helm lint ./plataforma-solicitudes
helm template ./plataforma-solicitudes            # render de verificación
helm install plataforma ./plataforma-solicitudes -n solicitudes --create-namespace \
  --set secret.existingSecret=plataforma-db-secret
```

## Manejo de secretos y promoción por ambientes

- **Secretos:** la contraseña de BD (y cualquier credencial) NO viven en `values.yaml`
  con valor real. En producción se crea un `Secret` fuera del chart (pipeline, Vault,
  Sealed Secrets, etc.) y se referencia con `--set secret.existingSecret=<nombre>`. El
  `Secret` de plantilla existe solo para desarrollo y usa un placeholder `CHANGE_ME`.
- **Promoción por ambientes:** un `values-<env>.yaml` por ambiente (dev/qa/prod) que
  sobreescribe imágenes (tags inmutables por release), `replicaCount`, `resources` y las
  URLs de infraestructura/issuer. Se despliega con
  `helm upgrade --install plataforma ./plataforma-solicitudes -f values-<env>.yaml`.
  El pipeline promueve la MISMA imagen (por digest) de un ambiente al siguiente.

## Valores clave

| Ruta | Descripción |
|---|---|
| `services.<svc>.image.repository/tag` | Imagen del servicio |
| `services.<svc>.replicaCount` | Réplicas |
| `services.<svc>.resources` | requests/limits CPU y memoria |
| `services.<svc>.config.*` | Env no sensible (datasource, kafka, keycloak) |
| `secret.existingSecret` | Nombre de un Secret externo (recomendado en prod) |
