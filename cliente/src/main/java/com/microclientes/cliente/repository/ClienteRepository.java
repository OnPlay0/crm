package com.microclientes.cliente.repository;

import com.microclientes.cliente.model.Cliente;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Aquí puedes agregar consultas personalizadas si las necesitas
    List<Cliente> findByUserId(Long userId);
    long countByUserId(Long userId);


    @Modifying
    @Transactional
    int deleteByUserId(Long userId);




}
