package com.micro.leads.service;

import com.micro.leads.dto.LeadDTO;
import com.micro.leads.model.Lead;
import com.micro.leads.repositories.LeadRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ModelMapper modelMapper;

    private Long getUserIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("No se encontró Authentication en SecurityContext");
        }

        String principal = authentication.getPrincipal().toString();

        try {
            return Long.parseLong(principal);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Principal inválido (no es numérico): " + principal);
        }
    }



    public List<LeadDTO> obtenerTodos() {
        Long userId = getUserIdFromContext();
        List<Lead> leads = leadRepository.findByUserId(userId);
        return leads.stream()
                .map(lead -> modelMapper.map(lead, LeadDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<LeadDTO> obtenerPorId(Long id) {
        Long userId = getUserIdFromContext();
        return leadRepository.findByIdAndUserId(id, userId)
                .map(lead -> modelMapper.map(lead, LeadDTO.class));
    }

    public List<LeadDTO> obtenerPorEstado(String estado) {
        Long userId = getUserIdFromContext();
        List<Lead> leads = leadRepository.findByEstado(estado).stream()
                .filter(lead -> lead.getUserId().equals(userId))
                .collect(Collectors.toList());

        return leads.stream()
                .map(lead -> modelMapper.map(lead, LeadDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public LeadDTO guardarLead(@Valid LeadDTO leadDTO) {
        Long userId = getUserIdFromContext();
        Lead lead = modelMapper.map(leadDTO, Lead.class);
        lead.setUserId(userId);  // Seteamos el userId
        Lead saved = leadRepository.save(lead);
        return modelMapper.map(saved, LeadDTO.class);
    }

    @Transactional
    public LeadDTO actualizarLead(Long id, @Valid LeadDTO leadDTO) {
        Long userId = getUserIdFromContext();

        return leadRepository.findByIdAndUserId(id, userId).map(lead -> {
            modelMapper.map(leadDTO, lead);
            Lead updated = leadRepository.save(lead);
            return modelMapper.map(updated, LeadDTO.class);
        }).orElseThrow(() -> new RuntimeException("Lead no encontrado o no autorizado para ID: " + id));
    }

    @Transactional
    public void eliminarLead(Long id) {
        Long userId = getUserIdFromContext();
        Lead lead = leadRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado o no autorizado para ID: " + id));
        leadRepository.delete(lead);
    }

    @Transactional
    public void borrarLeadsDelInvitado(Long userId) {
        int cantidad = leadRepository.deleteByUserId(userId);
    }

}
