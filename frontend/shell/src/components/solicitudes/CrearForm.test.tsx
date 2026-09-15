import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CrearForm } from "./CrearForm";

describe("CrearForm", () => {
  it("bloquea el envío y muestra errores Zod con datos inválidos", async () => {
    const onSubmit = vi.fn();
    render(
      <CrearForm categorias={[{ id: 1, nombre: "Soporte técnico", activo: true }]} onSubmit={onSubmit} />,
    );
    await userEvent.click(screen.getByRole("button", { name: /registrar solicitud/i }));
    expect(await screen.findByText(/al menos 5 caracteres/i)).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
