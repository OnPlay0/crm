package com.servicios.microservicios.repository;

import com.servicios.microservicios.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    @Query("""
    SELECT COALESCE(SUM(m.quantity),0)
    FROM InventoryMovement m
    WHERE m.userId=:uid AND m.item.id=:itemId
  """)
    Long balance(@Param("uid") Long uid, @Param("itemId") Long itemId);
}