import { get, SOLICITUDES_BASE } from "./client";
import type { Categoria } from "./types";

export const listarCategorias = () =>
  get<Categoria[]>(`${SOLICITUDES_BASE}/categorias`);
