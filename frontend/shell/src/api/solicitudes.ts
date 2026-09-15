import { get, post, SOLICITUDES_BASE } from "./client";
import type {
  AccionTransicion,
  CrearSolicitud,
  Estado,
  PageResult,
  SolicitudDetalle,
  SolicitudResumen,
} from "./types";

export interface FiltroBandeja {
  estado?: Estado | "";
  categoriaId?: number | "";
  page?: number;
  size?: number;
}

export function listarSolicitudes(filtro: FiltroBandeja): Promise<PageResult<SolicitudResumen>> {
  const p = new URLSearchParams();
  if (filtro.estado) p.set("estado", filtro.estado);
  if (filtro.categoriaId) p.set("categoriaId", String(filtro.categoriaId));
  p.set("page", String(filtro.page ?? 0));
  p.set("size", String(filtro.size ?? 10));
  return get<PageResult<SolicitudResumen>>(`${SOLICITUDES_BASE}/solicitudes?${p.toString()}`);
}

export const obtenerSolicitud = (id: number) =>
  get<SolicitudDetalle>(`${SOLICITUDES_BASE}/solicitudes/${id}`);

export const crearSolicitud = (cmd: CrearSolicitud) =>
  post<SolicitudDetalle>(`${SOLICITUDES_BASE}/solicitudes`, cmd);

export const tomarSolicitud = (id: number) =>
  post<SolicitudDetalle>(`${SOLICITUDES_BASE}/solicitudes/${id}/asignaciones`);

export const transicionarSolicitud = (
  id: number,
  accion: AccionTransicion,
  extra: { observacion?: string; motivo?: string } = {},
) => post<SolicitudDetalle>(`${SOLICITUDES_BASE}/solicitudes/${id}/transiciones`, { accion, ...extra });
