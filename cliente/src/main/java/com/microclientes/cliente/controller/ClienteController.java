package com.microclientes.cliente.controller;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "API de gestión de clientes")
public class ClienteController {
    private final ClienteService service;

    @Operation(summary = "Listar clientes con paginación")
    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> list(@ParameterObject Pageable page) {
        Page<ClienteDTO> result = service.list(page);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result);
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    public ClienteDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    @Operation(summary = "Crear cliente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO create(@Valid @RequestBody ClienteDTO dto) {
        return service.create(dto);
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ClienteDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Eliminar cliente")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
