package com.gestor.users.repository;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    // Podés agregar métodos personalizados si querés, por ahora no hace falta
}

