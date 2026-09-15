// Tipos de los módulos federados expuestos por el microfrontend remoto.
declare module "mfRemoto/IndicadoresPanel" {
  import type { ComponentType } from "react";
  export interface IndicadoresPanelProps {
    apiBase: string;
    token?: string;
  }
  const IndicadoresPanel: ComponentType<IndicadoresPanelProps>;
  export default IndicadoresPanel;
}
