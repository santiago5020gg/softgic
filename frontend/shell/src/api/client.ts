// Única capa con acceso HTTP. Inyecta el Bearer del token en memoria.
let authToken: string | undefined;

export function setAuthToken(token: string | undefined): void {
  authToken = token;
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const w = globalThis as unknown as {
  __SOLICITUDES_BASE__?: string;
  __INDICADORES_BASE__?: string;
};

export const SOLICITUDES_BASE = w.__SOLICITUDES_BASE__ ?? "http://localhost:18080/api/v1";
export const INDICADORES_BASE = w.__INDICADORES_BASE__ ?? "http://localhost:8082/api/v1";

export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set("Content-Type", "application/json");
  if (authToken) headers.set("Authorization", `Bearer ${authToken}`);

  const res = await fetch(url, { ...options, headers });

  if (!res.ok) {
    let message = res.statusText;
    try {
      const body = await res.json();
      message = body.message ?? message;
    } catch {
      /* respuesta sin cuerpo JSON */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export const get = <T>(url: string) => request<T>(url, { method: "GET" });
export const post = <T>(url: string, body?: unknown) =>
  request<T>(url, { method: "POST", body: body ? JSON.stringify(body) : undefined });
