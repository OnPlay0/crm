package com.gestor.users.dto;


import lombok.*;

import java.time.LocalDateTime;

@Data

public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt; // Si incluyes fechas
    private LocalDateTime updatedAt; // Si incluyes fechas


}
