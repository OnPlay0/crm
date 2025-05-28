package com.pipeline.ventas.controller;

import com.pipeline.ventas.dto.OportunidadVentaDTO;
import com.pipeline.ventas.model.OportunidadVenta;
import com.pipeline.ventas.repositories.OportunidadVentaRepository;
import com.pipeline.ventas.service.OportunidadVentaService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadVentaController {

    @Autowired
    private OportunidadVentaRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }


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
        return service.obtenerOportunidadPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OportunidadVentaDTO> crearOportunidad(@Valid @RequestBody OportunidadVentaDTO dto) {
        OportunidadVentaDTO creada = service.crearOportunidad(dto);
        return ResponseEntity.ok(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OportunidadVentaDTO> updateOportunidad(@PathVariable Long id, @Valid @RequestBody OportunidadVentaDTO dto) {
        try {
            OportunidadVentaDTO actualizada = service.updateOportunidad(id, dto);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOportunidad(@PathVariable Long id) {
        service.eliminarOportunidad(id);
        return ResponseEntity.noContent().build();
    }

    // 🟢 Total de ventas
    @GetMapping("/count")
    public ResponseEntity<Long> contarVentasUsuario() {
        Long userId = getUserIdFromContext();
        long total = repository.countByUserId(userId);
        return ResponseEntity.ok(total);
    }
    @GetMapping("/resumen-mensual")
    public ResponseEntity<Map<String, Double>> resumenVentasMensual() {
        Long userId = getUserIdFromContext();
        List<OportunidadVenta> ventas = repository.findByUserId(userId);

        Map<String, Double> resumen = ventas.stream()
                .collect(Collectors.groupingBy(
                        venta -> venta.getFechaCierreEstimada().getYear() + "-" +
                                String.format("%02d", venta.getFechaCierreEstimada().getMonthValue()),
                        Collectors.summingDouble(OportunidadVenta::getMonto)
                ));

        resumen.forEach((k, v) -> System.out.println("📊 " + k + " = " + v)); // DEBUG
        return ResponseEntity.ok(resumen);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> borrarVentasInvitado(@RequestHeader("X-User-Id") Long userId) {
        service.borrarVentasDelInvitado(userId);
        return ResponseEntity.noContent().build();
    }



}
