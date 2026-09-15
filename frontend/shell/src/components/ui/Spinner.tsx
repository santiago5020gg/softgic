import { Box, CircularProgress, Typography } from "@mui/material";

export interface SpinnerProps {
  label?: string;
}

/** Estado de carga reutilizable. */
export function Spinner({ label = "Cargando…" }: SpinnerProps) {
  return (
    <Box
      role="status"
      aria-live="polite"
      sx={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 2, p: 4 }}
    >
      <CircularProgress aria-hidden />
      <Typography color="text.secondary">{label}</Typography>
    </Box>
  );
}
