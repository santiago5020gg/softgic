import { Box, Typography, Divider } from "@mui/material";
import type { HistorialItem } from "../../api/types";

/** Línea de tiempo (historial de transiciones) — presentacional. */
export function LineaTiempo({ historial }: { historial: HistorialItem[] }) {
  return (
    <Box component="ol" sx={{ listStyle: "none", m: 0, p: 0 }} aria-label="Línea de tiempo">
      {historial.map((h, i) => (
        <Box component="li" key={i} sx={{ py: 1 }}>
          <Typography variant="subtitle2">
            {h.estadoAnterior ? `${h.estadoAnterior} → ` : ""}
            {h.estadoNuevo}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {new Date(h.ocurridoEn).toLocaleString()} · {h.actor}
            {h.motivo ? ` · ${h.motivo}` : ""}
          </Typography>
          {i < historial.length - 1 && <Divider sx={{ mt: 1 }} />}
        </Box>
      ))}
    </Box>
  );
}
