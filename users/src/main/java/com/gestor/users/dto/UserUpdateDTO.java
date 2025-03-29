package com.gestor.users.dto;

import lombok.Data;

// actualizar datos

@Data
public class UserUpdateDTO {
    private String username; // Opcional en la actualización
    private String email; // Opcional en la actualización
    private String role; // Opcional en la actualización
}