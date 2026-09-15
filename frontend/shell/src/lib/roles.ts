import type { Rol } from "../store/sesionSlice";

export const puedeTomar = (roles: Rol[]) => roles.includes("ANALISTA");
export const puedeResolver = (roles: Rol[]) => roles.includes("ANALISTA");
export const puedeDevolverCerrar = (roles: Rol[]) => roles.includes("SUPERVISOR");
export const puedeCrear = (roles: Rol[]) => roles.includes("SOLICITANTE");
