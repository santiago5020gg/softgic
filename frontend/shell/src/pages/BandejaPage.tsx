import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { listarSolicitudes, type FiltroBandeja } from "../api/solicitudes";
import { listarCategorias } from "../api/catalogo";
import type { Categoria, Estado, PageResult, SolicitudResumen } from "../api/types";
import { ApiError } from "../api/client";
import { SolicitudesTable } from "../components/solicitudes/SolicitudesTable";
import { Spinner } from "../components/ui/Spinner";
import { EmptyState } from "../components/ui/EmptyState";
import { ErrorState } from "../components/ui/ErrorState";
import { Paginador } from "../components/ui/Paginador";

const ESTADOS: Estado[] = ["REGISTRADA", "EN_ATENCION", "RESUELTA", "CERRADA"];

/** Contenedor: bandeja con filtros y paginación. */
export function BandejaPage() {
  const navigate = useNavigate();
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [filtro, setFiltro] = useState<FiltroBandeja>({ estado: "", categoriaId: "", page: 0, size: 10 });
  const [data, setData] = useState<PageResult<SolicitudResumen> | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    listarCategorias().then(setCategorias).catch(() => setCategorias([]));
  }, []);

  const cargar = useCallback(() => {
    setCargando(true);
    setError(null);
    listarSolicitudes(filtro)
      .then(setData)
      .catch((e) => setError(e instanceof ApiError ? e : new ApiError(0, "Error de red")))
      .finally(() => setCargando(false));
  }, [filtro]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  return (
    <Stack spacing={2}>
      <Typography variant="h5">Bandeja de solicitudes</Typography>
      <Paper sx={{ p: 2 }}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
          <TextField
            select
            label="Estado"
            size="small"
            value={filtro.estado}
            onChange={(e) => setFiltro((f) => ({ ...f, estado: e.target.value as Estado | "", page: 0 }))}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {ESTADOS.map((e) => (
              <MenuItem key={e} value={e}>
                {e}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Categoría"
            size="small"
            value={filtro.categoriaId}
            onChange={(e) =>
              setFiltro((f) => ({ ...f, categoriaId: e.target.value === "" ? "" : Number(e.target.value), page: 0 }))
            }
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todas</MenuItem>
            {categorias.map((c) => (
              <MenuItem key={c.id} value={c.id}>
                {c.nombre}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </Paper>

      <Paper>
        {cargando ? (
          <Spinner />
        ) : error ? (
          <ErrorState message={error.message} forbidden={error.status === 403} onRetry={cargar} />
        ) : !data || data.content.length === 0 ? (
          <EmptyState description="No hay solicitudes con esos filtros." />
        ) : (
          <>
            <SolicitudesTable solicitudes={data.content} onAbrir={(id) => navigate(`/solicitudes/${id}`)} />
            <Paginador
              page={data.page}
              totalPages={data.totalPages}
              onChange={(p) => setFiltro((f) => ({ ...f, page: p }))}
            />
          </>
        )}
      </Paper>
    </Stack>
  );
}
