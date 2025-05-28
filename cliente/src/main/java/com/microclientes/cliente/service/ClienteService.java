package com.microclientes.cliente.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.excepciones.ResourceNotFoundException;
import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }

    // Obtener todos los clientes del usuario autenticado
    public List<ClienteDTO> getAllClientesByUser() {
        Long userId = getUserIdFromContext();
        List<Cliente> clientes = clienteRepository.findByUserId(userId);
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .toList();
    }

    // Obtener cliente por ID SOLO si pertenece al usuario
    public Optional<ClienteDTO> getClienteByIdForUser(Long id) {
        Long userId = getUserIdFromContext();
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.filter(c -> c.getUserId().equals(userId))
                .map(c -> modelMapper.map(c, ClienteDTO.class));
    }

    // Crear cliente asignando el userId automáticamente
    @Transactional
    public ClienteDTO createCliente(@Valid ClienteDTO clienteDTO) {
        Long userId = getUserIdFromContext();
        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        cliente.setUserId(userId);  // 💡 Asignamos el userId autenticado
        Cliente savedCliente = clienteRepository.save(cliente);
        return modelMapper.map(savedCliente, ClienteDTO.class);
    }

    // Actualizar cliente solo si pertenece al usuario
    @Transactional
    public ClienteDTO updateCliente(Long id, @Valid ClienteDTO clienteDTO) {
        Long userId = getUserIdFromContext();
        Cliente cliente = clienteRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado o no autorizado"));

        modelMapper.map(clienteDTO, cliente);
        Cliente updatedCliente = clienteRepository.save(cliente);
        return modelMapper.map(updatedCliente, ClienteDTO.class);
    }

    // Eliminar cliente solo si pertenece al usuario
    @Transactional
    public void deleteCliente(Long id) {
        Long userId = getUserIdFromContext();
        Cliente cliente = clienteRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado o no autorizado"));

        clienteRepository.delete(cliente);
    }


    public void importarClientesDesdeArchivo(MultipartFile file, Long userId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        List<ClienteDTO> clienteDTOs = mapper.readValue(file.getInputStream(), new TypeReference<List<ClienteDTO>>() {});

        List<Cliente> clientes = clienteDTOs.stream()
                .map(dto -> {
                    Cliente cliente = modelMapper.map(dto, Cliente.class);
                    cliente.setUserId(userId); // 🔥 asignación para multitenancia
                    return cliente;
                })
                .toList();

        clienteRepository.saveAll(clientes);
    }

    @Transactional
    public void borrarClientesDelInvitado(Long userId) {
        int cantidad = clienteRepository.deleteByUserId(userId);
    }


}
