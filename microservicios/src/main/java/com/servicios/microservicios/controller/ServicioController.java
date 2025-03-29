package com.servicios.microservicios.controller;

import com.servicios.microservicios.dto.ServicioDTO;
import com.servicios.microservicios.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    public ServicioDTO crearServicio(@Valid @RequestBody ServicioDTO servicioDTO) {
        return servicioService.crearServicio(servicioDTO);
    }

    @GetMapping
    public List<ServicioDTO> obtenerServicios() {
        return servicioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ServicioDTO obtenerServicioPorId(@PathVariable Long id) {
        return servicioService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id " + id));
    }

    @PutMapping("/{id}")
    public ServicioDTO actualizarServicio(@PathVariable Long id,@Valid @RequestBody ServicioDTO servicioDTO) {
        return servicioService.actualizarServicio(id, servicioDTO);
    }

    @DeleteMapping("/{id}")
    public void eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
    }
}
