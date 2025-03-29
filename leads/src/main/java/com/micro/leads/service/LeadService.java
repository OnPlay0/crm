package com.micro.leads.service;

import com.micro.leads.dto.LeadDTO;
import com.micro.leads.model.Lead;
import com.micro.leads.repositories.LeadRepository;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<LeadDTO> obtenerTodos() {
        List<Lead> leads = leadRepository.findAll();
        return leads.stream()
                .map(lead -> modelMapper.map(lead, LeadDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<LeadDTO> obtenerPorId(Long id) {
        return leadRepository.findById(id)
                .map(lead -> modelMapper.map(lead, LeadDTO.class));
    }

    public List<LeadDTO> obtenerPorEstado(String estado) {
        List<Lead> leads = leadRepository.findByEstado(estado);
        return leads.stream()
                .map(lead -> modelMapper.map(lead, LeadDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public LeadDTO guardarLead(@Valid LeadDTO leadDTO) {
        System.out.println("📥 DTO recibido: estado = " + leadDTO.getEstado());
        Lead lead = modelMapper.map(leadDTO, Lead.class);
        System.out.println("✅ Entidad creada: estado = " + lead.getEstado());
        Lead saved = leadRepository.save(lead);
        return modelMapper.map(saved, LeadDTO.class);
    }

    @Transactional
    public LeadDTO actualizarLead(Long id, @Valid LeadDTO leadDTO) {
        return leadRepository.findById(id).map(lead -> {
            modelMapper.map(leadDTO, lead);
            Lead updated = leadRepository.save(lead);
            return modelMapper.map(updated, LeadDTO.class);
        }).orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminarLead(Long id) {
        leadRepository.deleteById(id);
    }
}
