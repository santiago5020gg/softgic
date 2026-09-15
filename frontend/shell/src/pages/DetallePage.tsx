import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Button,
  Chip,
  Divider,
  Grid,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import {
  obtenerSolicitud,
  tomarSolicitud,
  transicionarSolicitud,
} from "../api/solicitudes";
import type { SolicitudDetalle } from "../api/types";
import { ApiError } from "../api/client";
import { useAppSelector } from "../store/hooks";
import { puedeDevolverCerrar, puedeResolver, puedeTomar } from "../lib/roles";
import { Spinner } from "../components/ui/Spinner";
import { ErrorState } from "../components/ui/ErrorState";
import { EstadoChip } from "../components/ui/EstadoChip";
import { LineaTiempo } from "../components/solicitudes/LineaTiempo";

/** Contenedor: detalle con línea de tiempo y acciones según rol. */
export function DetallePage() {
  const { id } = useParams();
  const solicitudId = Number(id);
  const roles = useAppSelector((s) => s.sesion.roles);
  const [detalle, setDetalle] = useState<SolicitudDetalle | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [observacion, setObservacion] = useState("");

  const cargar = useCallback(() => {
    setCargando(true);
    setError(null);
    obtenerSolicitud(solicitudId)
      .then(setDetalle)
      .catch((e) => setError(e instanceof ApiError ? e : new ApiError(0, "Error de red")))
      .finally(() => setCargando(false));
  }, [solicitudId]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const ejecutar = (p: Promise<SolicitudDetalle>) => {
    p.then(setDetalle)
      .catch((e) => setError(e instanceof ApiError ? e : new ApiError(0, "Error de red")));
  };

  if (cargando) return <Spinner />;
  if (error && !detalle) return <ErrorState message={error.message} forbidden={error.status === 403} onRetry={cargar} />;
  if (!detalle) return null;

  const estado = detalle.estado;

  return (
    <Stack spacing={2}>
      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
        <Typography variant="h5">{detalle.codigo}</Typography>
        <EstadoChip estado={estado} />
        <Chip variant="outlined" size="small" label={detalle.prioridad} />
      </Stack>

      {error && <ErrorState message={error.message} forbidden={error.status === 403} />}

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 7 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6">{detalle.asunto}</Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              {detalle.descripcion}
            </Typography>
            <Divider sx={{ my: 2 }} />
            <Stack direction="row" spacing={2} flexWrap="wrap">
              {puedeTomar(roles) && estado === "REGISTRADA" && (
                <Button variant="contained" onClick={() => ejecutar(tomarSolicitud(solicitudId))}>
                  Tomar
                </Button>
              )}
              {puedeResolver(roles) && estado === "EN_ATENCION" && (
                <Button
                  variant="contained"
                  color="success"
                  onClick={() => ejecutar(transicionarSolicitud(solicitudId, "RESOLVER", { observacion }))}
                >
                  Resolver
                </Button>
              )}
              {puedeDevolverCerrar(roles) && estado === "RESUELTA" && (
                <>
                  <Button
                    variant="outlined"
                    onClick={() => ejecutar(transicionarSolicitud(solicitudId, "DEVOLVER", { motivo: observacion }))}
                  >
                    Devolver
                  </Button>
                  <Button
                    variant="contained"
                    onClick={() => ejecutar(transicionarSolicitud(solicitudId, "CERRAR"))}
                  >
                    Cerrar
                  </Button>
                </>
              )}
            </Stack>
            {(puedeResolver(roles) || puedeDevolverCerrar(roles)) && (
              <TextField
                label="Observación / motivo"
                value={observacion}
                onChange={(e) => setObservacion(e.target.value)}
                size="small"
                fullWidth
                sx={{ mt: 2 }}
              />
            )}
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Línea de tiempo
            </Typography>
            <LineaTiempo historial={detalle.historial} />
          </Paper>
        </Grid>
      </Grid>
    </Stack>
  );
}
