import { Chip } from "@mui/material";
import type { Estado } from "../../api/types";

const COLOR: Record<Estado, "default" | "info" | "warning" | "success"> = {
  REGISTRADA: "info",
  EN_ATENCION: "warning",
  RESUELTA: "success",
  CERRADA: "default",
};

const LABEL: Record<Estado, string> = {
  REGISTRADA: "Registrada",
  EN_ATENCION: "En atención",
  RESUELTA: "Resuelta",
  CERRADA: "Cerrada",
};

export function EstadoChip({ estado }: { estado: Estado }) {
  return <Chip size="small" color={COLOR[estado]} label={LABEL[estado]} />;
}
