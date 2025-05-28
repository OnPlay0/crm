package com.servicios.microservicios.controller;

import com.servicios.microservicios.dto.ServicioDTO;
import com.servicios.microservicios.repository.ServicioRepository;
import com.servicios.microservicios.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @Autowired
    private ServicioRepository repository;

    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }

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


    @GetMapping("/count")
    public ResponseEntity<Long> contarServiciosPorUsuario() {
        Long userId = getUserIdFromContext();
        long total = repository.countByUserId(userId);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> borrarServiciosInvitado(@RequestHeader("X-User-Id") Long userId) {
        servicioService.borrarServiciosDelInvitado(userId);
        return ResponseEntity.noContent().build();
    }

}
