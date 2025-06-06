package com.gestor.users.service;

import com.gestor.users.dto.AuthResponseDTO;
import com.gestor.users.dto.LoginUserDTO;
import com.gestor.users.dto.RegisterUserDTO;
import com.gestor.users.model.Role;
import com.gestor.users.model.RoleList;
import com.gestor.users.model.User;
import com.gestor.users.repository.RoleRepository;
import com.gestor.users.repository.UserRepository;
import com.gestor.users.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    public User signup(RegisterUserDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso.");
        }

        RoleList roleList = request.getRole();
        Role role = roleRepository.findByName(roleList)
                .orElseThrow(() -> new RuntimeException("El rol " + request.getRole() + " no existe"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        return userRepository.save(user);
    }

    public AuthResponseDTO authenticate(LoginUserDTO request) {
        System.out.println("🧪 Intentando autenticar: " + request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            System.out.println("✅ Autenticación pasó sin excepción.");
        } catch (Exception e) {
            System.out.println("❌ Falló la autenticación:");
            e.printStackTrace(); // 🔥 Esto es vital para Cloud Run
            throw e; // Dejalo que suba, pero que lo loguee
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("👤 Usuario encontrado: " + user.getUsername());

        String token = jwtUtils.createToken(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

        System.out.println("🔐 Token generado exitosamente.");

        return new AuthResponseDTO(token, token, user.getRole().getName().name());
    }


}
