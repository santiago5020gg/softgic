import { Component, lazy, Suspense, type ReactNode } from "react";
import { Typography, Stack } from "@mui/material";
import { Spinner } from "../components/ui/Spinner";
import { ErrorState } from "../components/ui/ErrorState";
import { INDICADORES_BASE } from "../api/client";
import { keycloak } from "../auth/keycloak";

// El panel de indicadores es un microfrontend REMOTO cargado por Module Federation.
const IndicadoresPanel = lazy(() => import("mfRemoto/IndicadoresPanel"));

class RemoteBoundary extends Component<{ children: ReactNode }, { error: boolean }> {
  state = { error: false };
  static getDerivedStateFromError() {
    return { error: true };
  }
  render() {
    if (this.state.error) {
      return (
        <ErrorState
          title="Microfrontend no disponible"
          message="No se pudo cargar el panel remoto de indicadores. Verifica que mf-remoto esté en ejecución."
        />
      );
    }
    return this.props.children;
  }
}

/** Contenedor: monta el microfrontend federado de indicadores. */
export function IndicadoresPage() {
  return (
    <Stack spacing={2}>
      <Typography variant="h5">Indicadores</Typography>
      <RemoteBoundary>
        <Suspense fallback={<Spinner label="Cargando panel remoto…" />}>
          <IndicadoresPanel apiBase={INDICADORES_BASE} token={keycloak.token} />
        </Suspense>
      </RemoteBoundary>
    </Stack>
  );
}
