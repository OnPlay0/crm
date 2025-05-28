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
    private final JwtUtils jwtUtils; // Usamos SOLO JwtUtils
    private final RoleRepository roleRepository;

    public User signup(RegisterUserDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        RoleList roleList = request.getRole();
        Role role = roleRepository.findByName(roleList)
                .orElseThrow(() -> new RuntimeException("El rol " + request.getRole() + " no existe"));
        user.setRole(role);

        return userRepository.save(user);
    }


    public User authenticate(LoginUserDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        return userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }


    public AuthResponseDTO refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido");
        }

        String refreshToken = authHeader.substring(7);
        String userEmail = jwtUtils.extractUserName(refreshToken); // ✅ CAMBIO acá

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!jwtUtils.validateToken(refreshToken, user)) { // ✅ CAMBIO acá
            throw new RuntimeException("Refresh token inválido");
        }

        String newAccessToken = jwtUtils.createToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())); // ✅ CAMBIO acá

        return new AuthResponseDTO(
                newAccessToken,
                refreshToken,
                user.getRole().getName().name()
        );
    }
}
