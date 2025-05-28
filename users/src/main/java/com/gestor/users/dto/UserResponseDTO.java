package com.gestor.users.dto;

import com.gestor.users.model.RoleList;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private RoleList role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
