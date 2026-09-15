import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      // El módulo federado no existe en el entorno de pruebas: se apunta a un stub.
      "mfRemoto/IndicadoresPanel": path.resolve(__dirname, "src/test/remoteStub.tsx"),
    },
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["./src/test/setup.ts"],
    css: false,
  },
});
