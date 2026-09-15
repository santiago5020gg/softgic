// Arranque STANDALONE del microfrontend: renderiza el panel por sí solo, con un
// campo para pegar un token de prueba (en modo federado el shell inyecta el token).
import { useState } from 'react';
import { createRoot } from 'react-dom/client';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import CssBaseline from '@mui/material/CssBaseline';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import IndicadoresPanel from './IndicadoresPanel';
import { DEFAULT_API_BASE } from './api';

const theme = createTheme();

function StandaloneApp() {
  const [tokenInput, setTokenInput] = useState('');
  const [token, setToken] = useState<string | undefined>(undefined);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container maxWidth="md" sx={{ py: 3 }}>
        <Typography variant="h4" gutterBottom>
          Microfrontend de indicadores (standalone)
        </Typography>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Ejecución independiente del shell. Pega un token JWT de Keycloak para consultar
          la API ({DEFAULT_API_BASE}).
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ my: 2 }}>
          <TextField
            fullWidth
            size="small"
            label="Bearer token"
            value={tokenInput}
            onChange={(e) => setTokenInput(e.target.value)}
          />
          <Button variant="contained" onClick={() => setToken(tokenInput || undefined)}>
            Cargar
          </Button>
        </Stack>
        <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
          <IndicadoresPanel token={token} />
        </Box>
      </Container>
    </ThemeProvider>
  );
}

const container = document.getElementById('root');
if (container) {
  createRoot(container).render(<StandaloneApp />);
}
