package com.pipeline.ventas.repositories;

import com.pipeline.ventas.model.OportunidadVenta;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OportunidadVentaRepository extends JpaRepository<OportunidadVenta, Long> {
    List<OportunidadVenta> findByUserId(Long userId);
    Optional<OportunidadVenta> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
    List<OportunidadVenta> findTop5ByUserIdOrderByFechaRegistroDesc(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OportunidadVenta o WHERE o.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);








}
