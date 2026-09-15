import Keycloak from "keycloak-js";
import type { Rol } from "../store/sesionSlice";

// Instancia única de Keycloak (OIDC Authorization Code + PKCE).
export const keycloak = new Keycloak({
  url: "http://localhost:8081",
  realm: "solicitudes",
  clientId: "solicitudes-shell",
});

const ROLES: Rol[] = ["SOLICITANTE", "ANALISTA", "SUPERVISOR"];

export interface SesionInfo {
  usuario: string;
  roles: Rol[];
}

/** Inicia sesión con PKCE. El token se mantiene EN MEMORIA (nunca en localStorage). */
export async function iniciarSesion(): Promise<SesionInfo> {
  await keycloak.init({
    onLoad: "login-required",
    pkceMethod: "S256",
    checkLoginIframe: false,
  });

  // Refresco automático del token en memoria.
  setInterval(() => {
    keycloak.updateToken(30).catch(() => keycloak.login());
  }, 20000);

  const claims = keycloak.tokenParsed as
    | { preferred_username?: string; realm_access?: { roles?: string[] } }
    | undefined;
  const roles = (claims?.realm_access?.roles ?? []).filter((r): r is Rol =>
    ROLES.includes(r as Rol),
  );
  return { usuario: claims?.preferred_username ?? "usuario", roles };
}

export const cerrarSesion = () =>
  keycloak.logout({ redirectUri: window.location.origin });
