package com.micro.leads.controller;

import com.micro.leads.dto.LeadDTO;
import com.micro.leads.repositories.LeadRepository;
import com.micro.leads.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@Validated
public class LeadController {

    private final LeadService leadService;

    @Autowired
    private LeadRepository repository;

    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }

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

    @GetMapping("/count")
    public ResponseEntity<Long> contarLeadsPorUsuario() {
        Long userId = getUserIdFromContext();
        long total = repository.countByUserId(userId);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> borrarLeadsInvitado(@RequestHeader("X-User-Id") Long userId) {
        leadService.borrarLeadsDelInvitado(userId);
        return ResponseEntity.noContent().build();
    }
}
