package com.microclientes.cliente.service;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.exception.ResourceNotFoundException;
import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.model.EstadoCliente;
import com.microclientes.cliente.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    private ClienteService service;
    private ClienteRepository repo;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        repo = mock(ClienteRepository.class);
        modelMapper = new ModelMapper();
        service = new ClienteService(repo, modelMapper);

        // userId en contexto (coincide con SecurityUtils)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("99", null)
        );
    }

    @Test
    void create_shouldSaveCliente() {
        ClienteDTO dto = new ClienteDTO();
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setEmail("juan@mail.com");
        dto.setEstado(EstadoCliente.ACTIVO);

        Cliente saved = new Cliente();
        saved.setId(1L);
        saved.setNombre("Juan");
        saved.setUserId(99L);
        saved.setFechaRegistro(LocalDateTime.now());
        saved.setEstado(EstadoCliente.ACTIVO);

        when(repo.existsByUserIdAndEmailIgnoreCaseAndDeletedAtIsNull(99L, "juan@mail.com")).thenReturn(false);
        when(repo.save(any())).thenReturn(saved);

        ClienteDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("Juan", result.getNombre());
    }

    @Test
    void get_shouldReturnCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setUserId(99L);
        cliente.setNombre("Ana");

        when(repo.findByIdAndUserIdAndDeletedAtIsNull(1L, 99L)).thenReturn(Optional.of(cliente));

        ClienteDTO result = service.get(1L);

        assertNotNull(result);
        assertEquals("Ana", result.getNombre());
    }

    @Test
    void get_shouldThrowIfNotFound() {
        when(repo.findByIdAndUserIdAndDeletedAtIsNull(1L, 99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.get(1L));
    }

    @Test
    void delete_shouldSoftDeleteAndSave() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setUserId(99L);
        c.setEmail("foo@bar.com");

        when(repo.findByIdAndUserIdAndDeletedAtIsNull(1L, 99L)).thenReturn(Optional.of(c));

        assertDoesNotThrow(() -> service.delete(1L));

        verify(repo).save(argThat(ent ->
                ent.getDeletedAt() != null &&
                        ent.getEmail() != null &&
                        ent.getEmail().contains("#deleted#")
        ));
        verify(repo, never()).softDelete(anyLong(), anyLong()); // por si en el futuro lo usás
    }

    @Test
    void delete_shouldThrowIfNotFound() {
        when(repo.findByIdAndUserIdAndDeletedAtIsNull(1L, 99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
        verify(repo, never()).save(any());
    }
}
