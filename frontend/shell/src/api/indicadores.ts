import { get, INDICADORES_BASE } from "./client";

export interface ConteoEstado {
  estado: string;
  total: number;
}
export interface ConteoCategoria {
  categoriaId: number;
  total: number;
}
export interface Resumen {
  porEstado: ConteoEstado[];
  porCategoria: ConteoCategoria[];
}
export interface PuntoTendencia {
  fecha: string;
  total: number;
}

export const obtenerResumen = () => get<Resumen>(`${INDICADORES_BASE}/indicadores/resumen`);
export const obtenerTendencia = () => get<PuntoTendencia[]>(`${INDICADORES_BASE}/indicadores/tendencia`);
