package com.microclientes.cliente.service;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.excepciones.ResourceNotFoundException;
import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.repository.ClienteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos los clientes
    public List<ClienteDTO> getAllClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteDTO.class))
                .toList(); // Convierte todas las entidades a DTOs
    }

    // Obtener un cliente por ID
    public Optional<ClienteDTO> getClienteById(Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.map(c -> modelMapper.map(c, ClienteDTO.class)); // Convierte la entidad a DTO
    }

    // Crear un nuevo cliente
    @Transactional
    public ClienteDTO createCliente(@Valid ClienteDTO clienteDTO) {
        System.out.println("Estado recibido en DTO: " + clienteDTO.getEstado());
        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class); // Mapea el DTO a entidad
        Cliente savedCliente = clienteRepository.save(cliente);
        return modelMapper.map(savedCliente, ClienteDTO.class); // Devuelve el DTO después de guardar
    }

    // Actualizar un cliente existente
    @Transactional
    public ClienteDTO updateCliente(Long id, @Valid ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        modelMapper.map(clienteDTO, cliente);
        Cliente updatedCliente = clienteRepository.save(cliente);
        return modelMapper.map(updatedCliente, ClienteDTO.class);
    }

    // Eliminar un cliente
    @Transactional
    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}
