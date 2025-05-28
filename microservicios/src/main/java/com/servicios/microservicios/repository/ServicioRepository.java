package com.servicios.microservicios.repository;

import com.servicios.microservicios.model.Servicio;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByUserId(Long userId);

    Optional<Servicio> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);


    @Modifying
    @Transactional
    int deleteByUserId(Long userId);


}