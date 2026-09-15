import { useState } from "react";
import {
  Box,
  Button,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import type { Categoria, CrearSolicitud, Prioridad } from "../../api/types";
import { crearSolicitudSchema } from "../../schemas/solicitud";

export interface CrearFormProps {
  categorias: Categoria[];
  enviando?: boolean;
  onSubmit: (data: CrearSolicitud) => void;
}

const PRIORIDADES: Prioridad[] = ["BAJA", "MEDIA", "ALTA"];

/** Formulario presentacional con validación Zod en la frontera. */
export function CrearForm({ categorias, enviando, onSubmit }: CrearFormProps) {
  const [asunto, setAsunto] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [categoriaId, setCategoriaId] = useState<number | "">("");
  const [prioridad, setPrioridad] = useState<Prioridad | "">("");
  const [errores, setErrores] = useState<Record<string, string>>({});

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsed = crearSolicitudSchema.safeParse({
      asunto,
      descripcion,
      categoriaId: categoriaId === "" ? undefined : Number(categoriaId),
      prioridad: prioridad === "" ? undefined : prioridad,
    });
    if (!parsed.success) {
      const errs: Record<string, string> = {};
      for (const issue of parsed.error.issues) {
        errs[String(issue.path[0])] = issue.message;
      }
      setErrores(errs);
      return;
    }
    setErrores({});
    onSubmit(parsed.data);
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Stack spacing={2} sx={{ maxWidth: 600 }}>
        <TextField
          label="Asunto"
          value={asunto}
          onChange={(e) => setAsunto(e.target.value)}
          error={!!errores.asunto}
          helperText={errores.asunto}
          fullWidth
        />
        <TextField
          label="Descripción"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          error={!!errores.descripcion}
          helperText={errores.descripcion}
          multiline
          minRows={3}
          fullWidth
        />
        <TextField
          select
          label="Categoría"
          value={categoriaId}
          onChange={(e) => setCategoriaId(Number(e.target.value))}
          error={!!errores.categoriaId}
          helperText={errores.categoriaId}
          fullWidth
        >
          {categorias.map((c) => (
            <MenuItem key={c.id} value={c.id}>
              {c.nombre}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label="Prioridad"
          value={prioridad}
          onChange={(e) => setPrioridad(e.target.value as Prioridad)}
          error={!!errores.prioridad}
          helperText={errores.prioridad}
          fullWidth
        >
          {PRIORIDADES.map((p) => (
            <MenuItem key={p} value={p}>
              {p}
            </MenuItem>
          ))}
        </TextField>
        <Button type="submit" variant="contained" disabled={enviando}>
          Registrar solicitud
        </Button>
      </Stack>
    </Box>
  );
}
