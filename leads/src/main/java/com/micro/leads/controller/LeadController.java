package com.micro.leads.controller;

import com.micro.leads.dto.LeadDTO;
import com.micro.leads.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@Validated
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadDTO> obtenerTodos() {
        return leadService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> obtenerPorId(@PathVariable Long id) {
        return leadService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public List<LeadDTO> obtenerPorEstado(@PathVariable String estado) {
        return leadService.obtenerPorEstado(estado);
    }

    @PostMapping
    public ResponseEntity<LeadDTO> guardarLead(@RequestBody @Valid LeadDTO leadDTO) {
        System.out.println("➡️ Estado recibido: " + leadDTO.getEstado());
        LeadDTO nuevoLead = leadService.guardarLead(leadDTO);
        return ResponseEntity.ok(nuevoLead);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadDTO> actualizarLead(@PathVariable Long id, @RequestBody @Valid LeadDTO leadDTO) {
        try {
            LeadDTO actualizado = leadService.actualizarLead(id, leadDTO);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLead(@PathVariable Long id) {
        leadService.eliminarLead(id);
        return ResponseEntity.noContent().build();
    }
}
