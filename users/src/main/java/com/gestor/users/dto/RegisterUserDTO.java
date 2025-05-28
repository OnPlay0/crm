package com.gestor.users.dto;

import com.gestor.users.model.RoleList;
import lombok.Data;

@Data
public class RegisterUserDTO {
    private String email;
    private String password;
    private String username;
    private RoleList role; // 🔥 AGREGAMOS ESTO
}
