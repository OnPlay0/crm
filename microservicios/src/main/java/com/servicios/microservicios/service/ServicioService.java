package com.servicios.microservicios.service;

import com.servicios.microservicios.dto.ServicioDTO;
import com.servicios.microservicios.model.Servicio;
import com.servicios.microservicios.repository.ServicioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ModelMapper modelMapper;

    private Long getUserIdFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String str && str.matches("\\d+")) {
            return Long.parseLong(str);
        }
        throw new RuntimeException("No se pudo extraer el userId del contexto");
    }


    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        Long userId = getUserIdFromContext();
        Servicio servicio = modelMapper.map(servicioDTO, Servicio.class);
        servicio.setUserId(userId);
        Servicio nuevoServicio = servicioRepository.save(servicio);
        return modelMapper.map(nuevoServicio, ServicioDTO.class);
    }

    public List<ServicioDTO> obtenerTodos() {
        Long userId = getUserIdFromContext();
        return servicioRepository.findByUserId(userId).stream()
                .map(servicio -> modelMapper.map(servicio, ServicioDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<ServicioDTO> obtenerPorId(Long id) {
        Long userId = getUserIdFromContext();
        return servicioRepository.findByIdAndUserId(id, userId)
                .map(servicio -> modelMapper.map(servicio, ServicioDTO.class));
    }

    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        Long userId = getUserIdFromContext();
        Servicio servicioExistente = servicioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado o no autorizado con id " + id));

        modelMapper.map(servicioDTO, servicioExistente);
        Servicio servicioActualizado = servicioRepository.save(servicioExistente);
        return modelMapper.map(servicioActualizado, ServicioDTO.class);
    }

    public void eliminarServicio(Long id) {
        Long userId = getUserIdFromContext();
        Servicio servicio = servicioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado o no autorizado con id " + id));
        servicioRepository.delete(servicio);
    }

    @Transactional
    public void borrarServiciosDelInvitado(Long userId) {
        int cantidad = servicioRepository.deleteByUserId(userId);
    }

}
