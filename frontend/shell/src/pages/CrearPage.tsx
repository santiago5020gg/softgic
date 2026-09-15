import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Snackbar, Stack, Typography } from "@mui/material";
import { CrearForm } from "../components/solicitudes/CrearForm";
import { listarCategorias } from "../api/catalogo";
import { crearSolicitud } from "../api/solicitudes";
import type { Categoria, CrearSolicitud } from "../api/types";
import { ApiError } from "../api/client";
import { Spinner } from "../components/ui/Spinner";
import { ErrorState } from "../components/ui/ErrorState";

/** Contenedor: creación de solicitud. */
export function CrearPage() {
  const navigate = useNavigate();
  const [categorias, setCategorias] = useState<Categoria[] | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    listarCategorias().then(setCategorias).catch((e) => setError(e as ApiError));
  }, []);

  const onSubmit = (data: CrearSolicitud) => {
    setEnviando(true);
    crearSolicitud(data)
      .then((s) => navigate(`/solicitudes/${s.id}`))
      .catch((e) => setError(e instanceof ApiError ? e : new ApiError(0, "Error de red")))
      .finally(() => setEnviando(false));
  };

  if (!categorias && !error) return <Spinner />;

  return (
    <Stack spacing={2}>
      <Typography variant="h5">Nueva solicitud</Typography>
      {error && error.status === 403 && <ErrorState forbidden />}
      <CrearForm categorias={categorias ?? []} enviando={enviando} onSubmit={onSubmit} />
      <Snackbar open={!!error && error.status !== 403} autoHideDuration={6000} onClose={() => setError(null)}>
        <Alert severity="error">{error?.message}</Alert>
      </Snackbar>
    </Stack>
  );
}
