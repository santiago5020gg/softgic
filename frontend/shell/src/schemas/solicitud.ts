import { z } from "zod";

// Validación de la frontera (formulario de creación) con Zod.
export const crearSolicitudSchema = z.object({
  asunto: z.string().min(5, "El asunto debe tener al menos 5 caracteres").max(200),
  descripcion: z.string().min(10, "La descripción debe tener al menos 10 caracteres").max(4000),
  categoriaId: z.number({ invalid_type_error: "Selecciona una categoría" }).int().positive("Selecciona una categoría"),
  prioridad: z.enum(["BAJA", "MEDIA", "ALTA"], { message: "Selecciona una prioridad" }),
});

export type CrearSolicitudInput = z.infer<typeof crearSolicitudSchema>;
