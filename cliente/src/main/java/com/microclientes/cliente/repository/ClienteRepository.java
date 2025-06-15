package com.microclientes.cliente.repository;

import com.microclientes.cliente.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Page<Cliente> findByUserId(Long userId, Pageable pageable);
    Optional<Cliente> findByIdAndUserId(Long id, Long userId);
    int deleteByUserIdAndId(Long userId, Long id);
}

