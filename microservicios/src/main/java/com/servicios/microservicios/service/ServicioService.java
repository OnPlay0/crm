package com.servicios.microservicios.service;

import com.servicios.microservicios.dto.ServicioDTO;
import com.servicios.microservicios.model.Servicio;
import com.servicios.microservicios.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ModelMapper modelMapper;

    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        Servicio servicio = modelMapper.map(servicioDTO, Servicio.class);
        Servicio nuevoServicio = servicioRepository.save(servicio);
        return modelMapper.map(nuevoServicio, ServicioDTO.class);
    }

    public List<ServicioDTO> obtenerTodos() {
        return servicioRepository.findAll().stream()
                .map(servicio -> modelMapper.map(servicio, ServicioDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<ServicioDTO> obtenerPorId(Long id) {
        return servicioRepository.findById(id)
                .map(servicio -> modelMapper.map(servicio, ServicioDTO.class));
    }

    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        Servicio servicioExistente = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id " + id));

        modelMapper.map(servicioDTO, servicioExistente);
        servicioExistente.setId(id);

        Servicio servicioActualizado = servicioRepository.save(servicioExistente);
        return modelMapper.map(servicioActualizado, ServicioDTO.class);
    }

    public void eliminarServicio(Long id) {
        servicioRepository.deleteById(id);
    }
}