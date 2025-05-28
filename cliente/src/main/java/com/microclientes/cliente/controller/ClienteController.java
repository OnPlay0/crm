package com.microclientes.cliente.controller;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.repository.ClienteRepository;
import com.microclientes.cliente.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/clientes")
@Validated
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository repository;

    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }


    // Obtener todos los clientes del usuario
    @GetMapping
    public List<ClienteDTO> getAllClientes() {
        return clienteService.getAllClientesByUser();
    }

    // Obtener un cliente por ID del usuario
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(@PathVariable Long id) {
        Optional<ClienteDTO> cliente = clienteService.getClienteByIdForUser(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    // Crear un nuevo cliente
    @PostMapping
    public ResponseEntity<ClienteDTO> createCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO nuevoCliente = clienteService.createCliente(clienteDTO);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    // Actualizar un cliente existente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        try {
            ClienteDTO actualizado = clienteService.updateCliente(id, clienteDTO);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar un cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/count")
    public ResponseEntity<Long> contarClientesPorUsuario() {
        Long userId = getUserIdFromContext();
        long total = repository.countByUserId(userId);
        return ResponseEntity.ok(total);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromContext(); // recuperamos el userId multitenant
            clienteService.importarClientesDesdeArchivo(file, userId);
            return ResponseEntity.ok("Clientes importados exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar archivo: " + e.getMessage());
        }
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> borrarClientesInvitado(@RequestHeader("X-User-Id") Long userId) {
        clienteService.borrarClientesDelInvitado(userId);
        return ResponseEntity.noContent().build();
    }
}
