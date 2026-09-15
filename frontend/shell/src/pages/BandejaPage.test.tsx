import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { store } from "../store";
import { BandejaPage } from "./BandejaPage";

describe("BandejaPage", () => {
  it("renderiza las solicitudes obtenidas de la API (MSW)", async () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <BandejaPage />
        </MemoryRouter>
      </Provider>,
    );
    expect(await screen.findByText("SOL-2026-000001")).toBeInTheDocument();
    expect(screen.getByText("Falla de impresora")).toBeInTheDocument();
  });
});
