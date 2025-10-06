package com.pipeline.ventas.repositories;

import com.pipeline.ventas.dto.DailyRevenueDTO;
import com.pipeline.ventas.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
   SELECT s FROM Sale s
   WHERE s.userId = :uid
     AND s.deletedAt IS NULL
     AND s.date >= :start AND s.date < :end
   ORDER BY s.date DESC, s.id DESC
""")
    Page<Sale> findByDateRange(@Param("uid") Long userId,
                               @Param("start") LocalDate start,
                               @Param("end") LocalDate end,
                               Pageable pageable);


    @Modifying
    @Query("UPDATE Sale s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id AND s.userId = :uid AND s.deletedAt IS NULL")
    int softDelete(@Param("uid") Long userId, @Param("id") Long saleId);

    @Query("""
  SELECT new com.pipeline.ventas.dto.DailyRevenueDTO(s.date, SUM(s.total))
  FROM Sale s
  WHERE s.userId = :uid AND s.date >= :start AND s.date < :end
  GROUP BY s.date ORDER BY s.date
""")
    List<DailyRevenueDTO> revenueByDay(@Param("uid") Long userId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);


    @Query("""
  SELECT COUNT(s), COALESCE(SUM(s.total),0)
  FROM Sale s
  WHERE s.userId = :uid AND s.date >= :start AND s.date < :end
""")
    Object[] salesAndRevenue(@Param("uid") Long uid, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 👉 Esto trae las N ventas más recientes ordenadas por fecha
    List<Sale> findTop5ByOrderByDateDesc();

    // 👉 Si querés hacerlo genérico (no fijo a 5), usás Pageable
    List<Sale> findAllByOrderByDateDesc(org.springframework.data.domain.Pageable pageable);
}
