export type Estado = "REGISTRADA" | "EN_ATENCION" | "RESUELTA" | "CERRADA";
export type Prioridad = "BAJA" | "MEDIA" | "ALTA";
export type AccionTransicion = "RESOLVER" | "DEVOLVER" | "CERRAR";

export interface Categoria {
  id: number;
  nombre: string;
  activo: boolean;
}

export interface HistorialItem {
  estadoAnterior: Estado | null;
  estadoNuevo: Estado;
  actor: string;
  motivo?: string | null;
  ocurridoEn: string;
}

export interface ObservacionItem {
  texto: string;
  actor: string;
  creadoEn: string;
}

export interface SolicitudResumen {
  id: number;
  codigo: string;
  asunto: string;
  categoriaId: number;
  prioridad: Prioridad;
  estado: Estado;
  solicitante: string;
  creadoEn: string;
}

export interface SolicitudDetalle extends SolicitudResumen {
  descripcion: string;
  analista?: string | null;
  actualizadoEn: string;
  historial: HistorialItem[];
  observaciones: ObservacionItem[];
}

export interface PageResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CrearSolicitud {
  asunto: string;
  descripcion: string;
  categoriaId: number;
  prioridad: Prioridad;
}
