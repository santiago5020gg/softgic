import { Box, Pagination } from "@mui/material";

export interface PaginadorProps {
  page: number; // base 0
  totalPages: number;
  onChange: (page: number) => void;
}

/** Paginación reutilizable (MUI Pagination es base 1; se traduce a base 0). */
export function Paginador({ page, totalPages, onChange }: PaginadorProps) {
  if (totalPages <= 1) return null;
  return (
    <Box sx={{ display: "flex", justifyContent: "center", p: 2 }}>
      <Pagination
        count={totalPages}
        page={page + 1}
        onChange={(_, p) => onChange(p - 1)}
        color="primary"
        aria-label="Paginación de solicitudes"
      />
    </Box>
  );
}
