package com.microclientes.cliente.service;

import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.exception.ResourceNotFoundException;
import com.microclientes.cliente.mapper.ClienteMapper;
import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {
    private final ClienteRepository repo;
    private final ClienteMapper mapper;

    private Long currentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext()
                .getAuthentication().getName());
    }

    public Page<ClienteDTO> list(Pageable page) {
        Long uid = currentUserId();
        log.info("Listando clientes página {} para usuario {}", page.getPageNumber(), uid);
        return repo.findByUserId(uid, page)
                .map(mapper::toDto);
    }

    public ClienteDTO get(Long id) {
        return repo.findByIdAndUserId(id, currentUserId())
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    @Transactional
    public ClienteDTO create(ClienteDTO dto) {
        Cliente c = mapper.toEntity(dto);
        c.setUserId(currentUserId());
        return mapper.toDto(repo.save(c));
    }

    @Transactional
    public ClienteDTO update(Long id, ClienteDTO dto) {
        Cliente existing = repo.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        mapper.updateEntityFromDto(dto, existing);
        return mapper.toDto(repo.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (repo.deleteByUserIdAndId(currentUserId(), id) == 0) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
    }
}
