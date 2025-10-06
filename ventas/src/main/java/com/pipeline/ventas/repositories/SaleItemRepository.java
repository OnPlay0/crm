package com.pipeline.ventas.repositories;

import com.pipeline.ventas.dto.FlatItemDTO;
import com.pipeline.ventas.dto.TopProductDTO;
import com.pipeline.ventas.model.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("""
      SELECT new com.pipeline.ventas.dto.FlatItemDTO(
        s.date, s.id,
        si.skuSnapshot, si.nameSnapshot, si.descriptionSnapshot,
        si.quantity, si.unitPrice, si.subtotal,
        s.customerId
      )
      FROM SaleItem si JOIN si.sale s
      WHERE s.userId = :uid
        AND s.date >= :start AND s.date < :end
      ORDER BY s.date DESC, s.id DESC, si.id
    """)
    List<FlatItemDTO> itemsByDateRange(@Param("uid") Long userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);


    @Query("""
  SELECT new com.pipeline.ventas.dto.TopProductDTO(
    si.skuSnapshot, si.nameSnapshot,
    SUM(si.quantity), SUM(si.subtotal)
  )
  FROM SaleItem si JOIN si.sale s
  WHERE s.userId = :uid AND s.date >= :start AND s.date < :end
  GROUP BY si.skuSnapshot, si.nameSnapshot
  ORDER BY SUM(si.subtotal) DESC
""")
    List<TopProductDTO> topProducts(
            @Param("uid") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable
    );

    @Query("""
  SELECT COALESCE(SUM(si.quantity),0), COALESCE(SUM(si.subtotal),0),
         COALESCE(SUM(CASE WHEN si.service = true THEN si.subtotal ELSE 0 END),0)
  FROM SaleItem si JOIN si.sale s
  WHERE s.userId = :uid AND s.date >= :start AND s.date < :end
""")
    Object[] itemsRevenueAndServices(@Param("uid") Long uid, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
