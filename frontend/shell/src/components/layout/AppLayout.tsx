import {
  AppBar,
  Box,
  Button,
  Container,
  Toolbar,
  Typography,
  Chip,
  Stack,
} from "@mui/material";
import { Link as RouterLink, Outlet, useNavigate } from "react-router-dom";
import { useAppSelector } from "../../store/hooks";
import { cerrarSesion } from "../../auth/keycloak";

/** Layout con barra de navegación, usuario/roles y logout. */
export function AppLayout() {
  const { usuario, roles } = useAppSelector((s) => s.sesion);
  const navigate = useNavigate();

  return (
    <Box sx={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <AppBar position="static">
        <Toolbar sx={{ gap: 2, flexWrap: "wrap" }}>
          <Typography variant="h6" sx={{ flexShrink: 0 }}>
            Solicitudes
          </Typography>
          <Stack direction="row" spacing={1} sx={{ flexGrow: 1 }}>
            <Button color="inherit" component={RouterLink} to="/bandeja">
              Bandeja
            </Button>
            <Button color="inherit" component={RouterLink} to="/crear">
              Nueva
            </Button>
            <Button color="inherit" component={RouterLink} to="/indicadores">
              Indicadores
            </Button>
          </Stack>
          <Stack direction="row" spacing={1} alignItems="center">
            <Typography variant="body2">{usuario}</Typography>
            {roles.map((r) => (
              <Chip key={r} size="small" color="secondary" label={r} />
            ))}
            <Button
              color="inherit"
              variant="outlined"
              size="small"
              onClick={() => cerrarSesion()}
            >
              Salir
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 3, flexGrow: 1 }}>
        <Outlet context={{ navigate }} />
      </Container>
    </Box>
  );
}
