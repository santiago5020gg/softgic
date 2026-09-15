import { createTheme } from "@mui/material/styles";

// Tema MUI base (institucional, sobrio). Responsive por defecto vía MUI.
export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#1f4e79" },
    secondary: { main: "#c8791f" },
    background: { default: "#f4f6f8" },
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: "Roboto, system-ui, Arial, sans-serif",
  },
});
