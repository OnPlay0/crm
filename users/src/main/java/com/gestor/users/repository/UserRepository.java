package com.gestor.users.repository;

import com.gestor.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // para login
    Optional<User> findByUsername(String username); // si lo necesitás
    Boolean existsByUsername(String username);




}

