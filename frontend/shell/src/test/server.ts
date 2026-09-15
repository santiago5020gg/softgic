import { setupServer } from "msw/node";
import { http, HttpResponse } from "msw";

const BASE = "http://localhost:18080/api/v1";

export const server = setupServer(
  http.get(`${BASE}/categorias`, () =>
    HttpResponse.json([{ id: 1, nombre: "Soporte técnico", activo: true }]),
  ),
  http.get(`${BASE}/solicitudes`, () =>
    HttpResponse.json({
      content: [
        {
          id: 1,
          codigo: "SOL-2026-000001",
          asunto: "Falla de impresora",
          categoriaId: 1,
          prioridad: "ALTA",
          estado: "REGISTRADA",
          solicitante: "ana.solicitante",
          creadoEn: "2026-09-15T10:00:00",
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    }),
  ),
);
