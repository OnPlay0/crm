package com.gestor.users.controller;

import com.gestor.users.model.Role;
import com.gestor.users.model.RoleList;
import com.gestor.users.model.User;
import com.gestor.users.repository.RoleRepository;
import com.gestor.users.dto.PasswordUpdateDTO;
import com.gestor.users.dto.UserCreateDTO;
import com.gestor.users.dto.UserResponseDTO;
import com.gestor.users.dto.UserUpdateDTO;
import com.gestor.users.repository.UserRepository;
import com.gestor.users.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader HttpHeaders headers) {
        System.out.println("🔍 Headers recibidos en servicio-users:");
        headers.forEach((key, value) -> System.out.println("🔸 " + key + " = " + value));

        String userId = headers.getFirst("X-User-Id");
        String role = headers.getFirst("X-Role");

        if (userId == null || !userId.matches("\\d+")) {
            return ResponseEntity.status(403).body("ID de usuario inválido");
        }

        Optional<User> optionalUser = userService.getUserById(Long.parseLong(userId));

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(403).body("Usuario no encontrado");
        }

        if (!RoleList.ROLE_ADMIN.name().equals(role)) {
            return ResponseEntity.status(403).body("Acceso denegado");
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestHeader HttpHeaders headers,
                                        @Valid @RequestBody UserCreateDTO userCreateDTO) {
        String roleHeader = headers.getFirst("X-Role");

        if (!RoleList.ROLE_ADMIN.name().equals(roleHeader)) {
            return ResponseEntity.status(403).body("Solo el administrador puede crear usuarios.");
        }

        try {
            RoleList roleList = userCreateDTO.getRole();
            if (roleList == null) {
                return ResponseEntity.badRequest().body("El rol no puede ser nulo.");
            }

            Role role = roleRepository.findByName(roleList)
                    .orElseThrow(() -> new RuntimeException("El rol " + roleList + " no existe."));

            User user = modelMapper.map(userCreateDTO, User.class);
            user.setRole(role);

            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(modelMapper.map(createdUser, UserResponseDTO.class));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Rol inválido: " + userCreateDTO.getRole());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error al crear el usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existingUser.get();
        modelMapper.map(userUpdateDTO, user);

        if (userUpdateDTO.getRole() != null) {
            RoleList roleList = userUpdateDTO.getRole();
            Role role = roleRepository.findByName(roleList)
                    .orElseThrow(() -> new RuntimeException("El rol " + roleList + " no existe"));
            user.setRole(role);
        }

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(modelMapper.map(updatedUser, UserResponseDTO.class));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateDTO passwordDTO) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPassword(passwordDTO.getPassword());
            userService.updateUser(id, user);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> borrarDatosDelInvitado(@RequestHeader("X-User-Id") Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            userRepository.deleteById(userId);
        });
        return ResponseEntity.noContent().build();
    }



}
