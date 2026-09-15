import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export type Rol = "SOLICITANTE" | "ANALISTA" | "SUPERVISOR";

export interface SesionState {
  autenticado: boolean;
  usuario: string | null;
  roles: Rol[];
}

const initialState: SesionState = {
  autenticado: false,
  usuario: null,
  roles: [],
};

const sesionSlice = createSlice({
  name: "sesion",
  initialState,
  reducers: {
    sesionIniciada(state, action: PayloadAction<{ usuario: string; roles: Rol[] }>) {
      state.autenticado = true;
      state.usuario = action.payload.usuario;
      state.roles = action.payload.roles;
    },
    sesionCerrada(state) {
      state.autenticado = false;
      state.usuario = null;
      state.roles = [];
    },
  },
});

export const { sesionIniciada, sesionCerrada } = sesionSlice.actions;
export default sesionSlice.reducer;
