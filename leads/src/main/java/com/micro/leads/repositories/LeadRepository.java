package com.micro.leads.repositories;


import com.micro.leads.model.Lead;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByEstado(String estado);

    List<Lead> findByUserId(Long userId);
    Optional<Lead> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Transactional
    int deleteByUserId(Long userId);


}
