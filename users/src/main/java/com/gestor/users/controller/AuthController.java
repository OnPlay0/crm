package com.gestor.users.controller;

import com.gestor.users.dto.AuthResponseDTO;
import com.gestor.users.dto.LoginUserDTO;
import com.gestor.users.dto.RegisterUserDTO;
import com.gestor.users.model.RoleList;
import com.gestor.users.model.User;
import com.gestor.users.service.AuthService;
import com.gestor.users.util.JwtUtils; // 👈 este es el import correcto
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils; // 👈 CAMBIO: ahora inyectamos JwtUtils (no JwtService)

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO request) {
        request.setRole(RoleList.ROLE_USER);
        User registeredUser = authService.signup(request);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginUserDTO request) {
        AuthResponseDTO response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }


}
