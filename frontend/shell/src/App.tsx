import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/layout/AppLayout";
import { BandejaPage } from "./pages/BandejaPage";
import { CrearPage } from "./pages/CrearPage";
import { DetallePage } from "./pages/DetallePage";
import { IndicadoresPage } from "./pages/IndicadoresPage";

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/bandeja" replace />} />
          <Route path="/bandeja" element={<BandejaPage />} />
          <Route path="/crear" element={<CrearPage />} />
          <Route path="/solicitudes/:id" element={<DetallePage />} />
          <Route path="/indicadores" element={<IndicadoresPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
