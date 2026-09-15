package com.gov.solicitudes.infrastructure.adapter.in;

import com.gov.solicitudes.core.dto.CategoriaDto;
import com.gov.solicitudes.core.ports.in.CatalogoServicePort;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adapter de entrada REST para el catálogo de categorías (solo lectura). */
@RestController
@RequestMapping("/api/v1/categorias")
public class CatalogoController {

    private final CatalogoServicePort service;

    public CatalogoController(CatalogoServicePort service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDto> listar() {
        return service.listarActivas();
    }
}
