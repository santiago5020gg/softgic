import type { Meta, StoryObj } from "@storybook/react";
import { Button } from "@mui/material";
import { EmptyState } from "./EmptyState";

const meta: Meta<typeof EmptyState> = {
  title: "UI/EmptyState",
  component: EmptyState,
};
export default meta;

type Story = StoryObj<typeof EmptyState>;

export const PorDefecto: Story = {};

export const ConDescripcion: Story = {
  args: {
    title: "No hay solicitudes",
    description: "Aún no se han registrado solicitudes con esos filtros.",
  },
};

export const ConAccion: Story = {
  args: {
    title: "Bandeja vacía",
    description: "Crea la primera solicitud.",
    action: <Button variant="contained">Nueva solicitud</Button>,
  },
};
