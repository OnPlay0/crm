package com.pipeline.ventas.service;

import com.pipeline.ventas.dto.OportunidadVentaDTO;
import com.pipeline.ventas.model.OportunidadVenta;
import com.pipeline.ventas.repositories.OportunidadVentaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OportunidadVentaService {

    private final OportunidadVentaRepository repository;
    private final ModelMapper modelMapper;

    public OportunidadVentaService(OportunidadVentaRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public List<OportunidadVentaDTO> listarOportunidades() {
        return repository.findAll().stream()
                .map(o -> modelMapper.map(o, OportunidadVentaDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<OportunidadVentaDTO> obtenerOportunidadPorId(Long id) {
        return repository.findById(id)
                .map(o -> modelMapper.map(o, OportunidadVentaDTO.class));
    }

    public OportunidadVentaDTO crearOportunidad(OportunidadVentaDTO dto) {
        OportunidadVenta oportunidad = modelMapper.map(dto, OportunidadVenta.class);
        oportunidad.setFechaCreacion(java.time.LocalDate.now());
        return modelMapper.map(repository.save(oportunidad), OportunidadVentaDTO.class);
    }

    public OportunidadVentaDTO updateOportunidad(Long id, OportunidadVentaDTO dto) {
        OportunidadVenta existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada"));

        // Se mapean solo los campos del DTO sobre la entidad existente (sin reemplazar ID ni campos controlados por JPA)
        modelMapper.map(dto, existente);

        return modelMapper.map(repository.save(existente), OportunidadVentaDTO.class);
    }

    public void eliminarOportunidad(Long id) {
        repository.deleteById(id);
    }
}
