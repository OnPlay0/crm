package com.pipeline.ventas.service;

import com.pipeline.ventas.dto.OportunidadVentaDTO;
import com.pipeline.ventas.model.OportunidadVenta;
import com.pipeline.ventas.repositories.OportunidadVentaRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OportunidadVentaService {

    @Autowired
    private OportunidadVentaRepository repository;

    @Autowired
    private ModelMapper modelMapper;


    private Long getUserIdFromContext() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userIdStr);
    }

    public List<OportunidadVentaDTO> listarOportunidades() {
        Long userId = getUserIdFromContext();
        return repository.findByUserId(userId).stream()
                .map(venta -> modelMapper.map(venta, OportunidadVentaDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<OportunidadVentaDTO> obtenerOportunidadPorId(Long id) {
        Long userId = getUserIdFromContext();
        return repository.findByIdAndUserId(id, userId)
                .map(venta -> modelMapper.map(venta, OportunidadVentaDTO.class));
    }

    @Transactional
    public OportunidadVentaDTO crearOportunidad(@Valid OportunidadVentaDTO dto) {
        Long userId = getUserIdFromContext();
        OportunidadVenta venta = modelMapper.map(dto, OportunidadVenta.class);
        venta.setUserId(userId);
        OportunidadVenta saved = repository.save(venta);
        return modelMapper.map(saved, OportunidadVentaDTO.class);
    }

    @Transactional
    public OportunidadVentaDTO updateOportunidad(Long id, @Valid OportunidadVentaDTO dto) {
        Long userId = getUserIdFromContext();

        return repository.findByIdAndUserId(id, userId).map(venta -> {
            modelMapper.map(dto, venta);
            OportunidadVenta updated = repository.save(venta);
            return modelMapper.map(updated, OportunidadVentaDTO.class);
        }).orElseThrow(() -> new RuntimeException("Venta no encontrada o no autorizada para ID: " + id));
    }

    @Transactional
    public void eliminarOportunidad(Long id) {
        Long userId = getUserIdFromContext();
        OportunidadVenta venta = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada o no autorizada para ID: " + id));
        repository.delete(venta);
    }

    @Transactional
    public void borrarVentasDelInvitado(Long userId) {
        int cantidad = repository.deleteByUserId(userId);
    }

}
