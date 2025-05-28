package com.gestor.users.dto;


import com.gestor.users.model.RoleList;
import lombok.*;

import java.time.LocalDateTime;

@Data

public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private RoleList role;
    private LocalDateTime createdAt; // Si incluyes fechas
    private LocalDateTime updatedAt; // Si incluyes fechas


}
