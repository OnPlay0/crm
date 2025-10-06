package com.servicios.microservicios.repository;


import com.servicios.microservicios.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {

    Page<CatalogItem> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("""
    SELECT c FROM CatalogItem c
     WHERE c.userId = :uid AND c.deletedAt IS NULL
       AND (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%'))
                       OR LOWER(c.sku)  LIKE LOWER(CONCAT('%',:q,'%')))
       AND (:type IS NULL OR c.type = :type)
       AND (:cat  IS NULL OR LOWER(c.category) LIKE LOWER(CONCAT('%',:cat,'%')))
       AND (:active IS NULL OR c.active = :active)
  """)
    Page<CatalogItem> search(@Param("uid") Long uid,
                             @Param("q") String q,
                             @Param("type") ItemType type,
                             @Param("cat") String category,
                             @Param("active") Boolean active,
                             Pageable pageable);

    Optional<CatalogItem> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    boolean existsByUserIdAndSkuIgnoreCaseAndDeletedAtIsNull(Long userId, String sku);
    Long countByUserIdAndDeletedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE CatalogItem c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id=:id AND c.userId=:uid AND c.deletedAt IS NULL")
    int softDelete(@Param("uid") Long uid, @Param("id") Long id);

    List<CatalogItem> findAllByUserIdAndTypeAndDeletedAtIsNullOrderByIdDesc(Long userId, ItemType type);


}
