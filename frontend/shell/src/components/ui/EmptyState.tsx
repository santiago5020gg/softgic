import { Box, Typography } from "@mui/material";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";
import type { ReactNode } from "react";

export interface EmptyStateProps {
  title?: string;
  description?: string;
  action?: ReactNode;
}

/** Estado vacío reutilizable. */
export function EmptyState({
  title = "Sin resultados",
  description = "No hay elementos para mostrar.",
  action,
}: EmptyStateProps) {
  return (
    <Box sx={{ textAlign: "center", p: 6, color: "text.secondary" }}>
      <InboxOutlinedIcon sx={{ fontSize: 48, opacity: 0.5 }} aria-hidden />
      <Typography variant="h6" sx={{ mt: 1 }}>
        {title}
      </Typography>
      <Typography variant="body2">{description}</Typography>
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Box>
  );
}
