// Capa HTTP tipada del microfrontend: único lugar donde viven fetch y endpoints.

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

async function get<T>(url: string, token?: string): Promise<T> {
  const res = await fetch(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return (await res.json()) as T;
}

export const DEFAULT_API_BASE = 'http://localhost:8082/api/v1';

export function fetchResumen(apiBase: string, token?: string): Promise<Resumen> {
  return get<Resumen>(`${apiBase}/indicadores/resumen`, token);
}

export function fetchTendencia(apiBase: string, token?: string): Promise<PuntoTendencia[]> {
  return get<PuntoTendencia[]>(`${apiBase}/indicadores/tendencia`, token);
}
