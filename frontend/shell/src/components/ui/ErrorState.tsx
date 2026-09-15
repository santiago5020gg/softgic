import { Alert, AlertTitle, Box, Button } from "@mui/material";

export interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  /** true cuando el fallo es 403 (autorización insuficiente). */
  forbidden?: boolean;
}

/** Estado de error / autorización insuficiente reutilizable. */
export function ErrorState({ title, message, onRetry, forbidden }: ErrorStateProps) {
  return (
    <Box sx={{ p: 3 }}>
      <Alert
        severity={forbidden ? "warning" : "error"}
        action={
          onRetry ? (
            <Button color="inherit" size="small" onClick={onRetry}>
              Reintentar
            </Button>
          ) : undefined
        }
      >
        <AlertTitle>
          {title ?? (forbidden ? "Autorización insuficiente" : "Ocurrió un error")}
        </AlertTitle>
        {message ?? (forbidden ? "No tienes permisos para esta acción." : "Intenta nuevamente.")}
      </Alert>
    </Box>
  );
}
