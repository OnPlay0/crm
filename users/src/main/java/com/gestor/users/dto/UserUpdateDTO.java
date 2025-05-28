package com.gestor.users.dto;

import com.gestor.users.model.RoleList;
import lombok.Data;

@Data
public class UserUpdateDTO {

    private String username;
    private String email;
    private String password;
    private RoleList role;
}
