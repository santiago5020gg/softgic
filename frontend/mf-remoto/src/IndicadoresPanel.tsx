import { useCallback, useEffect, useState } from 'react';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Typography from '@mui/material/Typography';
import {
  DEFAULT_API_BASE,
  fetchResumen,
  fetchTendencia,
  type PuntoTendencia,
  type Resumen,
} from './api';

export interface IndicadoresPanelProps {
  /** Base de la API de indicadores. El shell la inyecta cuando va federado. */
  apiBase?: string;
  /** JWT de Keycloak. Sin token, la API responde 401. */
  token?: string;
}

type Estado = 'cargando' | 'ok' | 'error';

/**
 * Panel analítico reutilizable expuesto por el microfrontend remoto.
 * Presenta solicitudes por estado, por categoría y la tendencia diaria.
 */
export default function IndicadoresPanel({ apiBase = DEFAULT_API_BASE, token }: IndicadoresPanelProps) {
  const [estado, setEstado] = useState<Estado>('cargando');
  const [error, setError] = useState<string>('');
  const [resumen, setResumen] = useState<Resumen | null>(null);
  const [tendencia, setTendencia] = useState<PuntoTendencia[]>([]);

  const cargar = useCallback(async () => {
    setEstado('cargando');
    setError('');
    try {
      const [r, t] = await Promise.all([
        fetchResumen(apiBase, token),
        fetchTendencia(apiBase, token),
      ]);
      setResumen(r);
      setTendencia(t);
      setEstado('ok');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error desconocido');
      setEstado('error');
    }
  }, [apiBase, token]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  if (estado === 'cargando') {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress aria-label="Cargando indicadores" />
      </Box>
    );
  }

  if (estado === 'error') {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        No se pudieron cargar los indicadores ({error}).
        {!token && ' Falta un token de acceso válido.'}
      </Alert>
    );
  }

  const totalTendencia = tendencia.reduce((acc, p) => acc + p.total, 0);
  const sinDatos =
    (resumen?.porEstado.length ?? 0) === 0 && (resumen?.porCategoria.length ?? 0) === 0;

  if (sinDatos) {
    return (
      <Alert severity="info" sx={{ m: 2 }}>
        Aún no hay solicitudes registradas para mostrar indicadores.
      </Alert>
    );
  }

  return (
    <Box sx={{ p: { xs: 1, sm: 2 } }}>
      <Typography variant="h5" gutterBottom>
        Indicadores operacionales
      </Typography>
      <Stack
        direction={{ xs: 'column', md: 'row' }}
        spacing={2}
        sx={{ mb: 2 }}
        useFlexGap
        flexWrap="wrap"
      >
        <Card sx={{ flex: 1, minWidth: 260 }}>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Solicitudes por estado
            </Typography>
            <Table size="small" aria-label="Solicitudes por estado">
              <TableHead>
                <TableRow>
                  <TableCell>Estado</TableCell>
                  <TableCell align="right">Total</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {resumen?.porEstado.map((c) => (
                  <TableRow key={c.estado}>
                    <TableCell>{c.estado}</TableCell>
                    <TableCell align="right">{c.total}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card sx={{ flex: 1, minWidth: 260 }}>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              Solicitudes por categoría
            </Typography>
            <Table size="small" aria-label="Solicitudes por categoría">
              <TableHead>
                <TableRow>
                  <TableCell>Categoría (id)</TableCell>
                  <TableCell align="right">Total</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {resumen?.porCategoria.map((c) => (
                  <TableRow key={c.categoriaId}>
                    <TableCell>{c.categoriaId}</TableCell>
                    <TableCell align="right">{c.total}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </Stack>

      <Card>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Tendencia diaria
          </Typography>
          {tendencia.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Sin datos de tendencia.
            </Typography>
          ) : (
            <Stack spacing={1}>
              {tendencia.map((p) => (
                <Box key={p.fecha}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">{p.fecha}</Typography>
                    <Typography variant="body2">{p.total}</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={totalTendencia ? Math.round((p.total / totalTendencia) * 100) : 0}
                    aria-label={`Tendencia ${p.fecha}`}
                  />
                </Box>
              ))}
            </Stack>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
