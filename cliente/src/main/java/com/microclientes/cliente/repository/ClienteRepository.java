package com.microclientes.cliente.repository;

import com.microclientes.cliente.model.Cliente;
import com.microclientes.cliente.model.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Page<Cliente> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<Cliente> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Long countByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByUserIdAndEmailIgnoreCaseAndDeletedAtIsNull(Long userId, String email);

    List<Cliente> findByUserIdAndDeletedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE Cliente c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id AND c.userId = :userId AND c.deletedAt IS NULL")
    int softDelete(@Param("userId") Long userId, @Param("id") Long id);

    @Query("""
     SELECT c FROM Cliente c
     WHERE c.userId = :userId AND c.deletedAt IS NULL
       AND (:nombre   IS NULL OR LOWER(c.nombre)   LIKE LOWER(CONCAT('%', :nombre,   '%')))
       AND (:apellido IS NULL OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')))
       AND (:email    IS NULL OR LOWER(c.email)    LIKE LOWER(CONCAT('%', :email,    '%')))
       AND (:estado   IS NULL OR c.estado = :estado)
  """)
    Page<Cliente> buscarPorFiltros(@Param("userId") Long userId,
                                   @Param("nombre") String nombre,
                                   @Param("apellido") String apellido,
                                   @Param("email") String email,
                                   @Param("estado") EstadoCliente estado,
                                   Pageable pageable);

    @Query("""
     SELECT DISTINCT c
       FROM Cliente c
       LEFT JOIN FETCH c.direccion d
      WHERE c.userId = :uid
        AND c.deletedAt IS NULL
      ORDER BY c.id ASC
  """)
    List<Cliente> exportAllByUserId(@Param("uid") Long uid);
}
