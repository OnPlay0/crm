package com.servicios.microservicios.repository;

import com.servicios.microservicios.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {}