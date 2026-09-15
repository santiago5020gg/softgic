import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { CssBaseline, ThemeProvider } from "@mui/material";
import { store } from "./store";
import { sesionIniciada } from "./store/sesionSlice";
import { theme } from "./theme";
import { App } from "./App";
import { iniciarSesion, keycloak } from "./auth/keycloak";
import { setAuthToken } from "./api/client";

async function main() {
  const root = createRoot(document.getElementById("root")!);
  try {
    const sesion = await iniciarSesion();
    setAuthToken(keycloak.token);
    keycloak.onAuthRefreshSuccess = () => setAuthToken(keycloak.token);
    store.dispatch(sesionIniciada(sesion));
    root.render(
      <StrictMode>
        <Provider store={store}>
          <ThemeProvider theme={theme}>
            <CssBaseline />
            <App />
          </ThemeProvider>
        </Provider>
      </StrictMode>,
    );
  } catch {
    root.render(<div style={{ padding: 24 }}>No se pudo iniciar sesión. Recarga la página.</div>);
  }
}

main();
