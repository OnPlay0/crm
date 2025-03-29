package com.pipeline.ventas.controller;

import com.pipeline.ventas.dto.OportunidadVentaDTO;
import com.pipeline.ventas.service.OportunidadVentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadVentaController {

    private final OportunidadVentaService service;

    public OportunidadVentaController(OportunidadVentaService service) {
        this.service = service;
    }

    @GetMapping
    public List<OportunidadVentaDTO> listarOportunidades() {
        return service.listarOportunidades();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OportunidadVentaDTO> obtenerOportunidad(@PathVariable Long id) {
        Optional<OportunidadVentaDTO> oportunidad = service.obtenerOportunidadPorId(id);
        return oportunidad.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OportunidadVentaDTO> crearOportunidad(@Valid @RequestBody OportunidadVentaDTO dto) {
        OportunidadVentaDTO nuevaOportunidad = service.crearOportunidad(dto);
        return ResponseEntity.ok(nuevaOportunidad);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OportunidadVentaDTO> updateOportunidad(@PathVariable Long id, @Valid @RequestBody OportunidadVentaDTO oportunidadDTO) {
        try {
            OportunidadVentaDTO actualizado = service.updateOportunidad(id, oportunidadDTO);

            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOportunidad(@PathVariable Long id) {
        service.eliminarOportunidad(id);
        return ResponseEntity.noContent().build();
    }
}
