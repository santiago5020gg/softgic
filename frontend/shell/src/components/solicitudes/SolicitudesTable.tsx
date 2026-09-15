import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Chip,
} from "@mui/material";
import type { SolicitudResumen } from "../../api/types";
import { EstadoChip } from "../ui/EstadoChip";

export interface SolicitudesTableProps {
  solicitudes: SolicitudResumen[];
  onAbrir: (id: number) => void;
}

/** Componente presentacional: recibe datos y un callback por props (sin fetch). */
export function SolicitudesTable({ solicitudes, onAbrir }: SolicitudesTableProps) {
  return (
    <Table size="small" aria-label="Bandeja de solicitudes">
      <TableHead>
        <TableRow>
          <TableCell>Código</TableCell>
          <TableCell>Asunto</TableCell>
          <TableCell>Prioridad</TableCell>
          <TableCell>Estado</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {solicitudes.map((s) => (
          <TableRow
            key={s.id}
            hover
            tabIndex={0}
            role="button"
            sx={{ cursor: "pointer" }}
            onClick={() => onAbrir(s.id)}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                onAbrir(s.id);
              }
            }}
          >
            <TableCell>{s.codigo}</TableCell>
            <TableCell>{s.asunto}</TableCell>
            <TableCell>
              <Chip size="small" variant="outlined" label={s.prioridad} />
            </TableCell>
            <TableCell>
              <EstadoChip estado={s.estado} />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
