package com.gestor.users.controller;

import com.gestor.users.dto.PasswordUpdateDTO;
import com.gestor.users.dto.UserCreateDTO;
import com.gestor.users.dto.UserResponseDTO;
import com.gestor.users.dto.UserUpdateDTO;
import com.gestor.users.model.User;
import com.gestor.users.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        User createdUser = userService.createUser(user);
        return modelMapper.map(createdUser, UserResponseDTO.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userDTO) {
        Optional<User> existingUser = userService.getUserById(id);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            modelMapper.map(userDTO, user);
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(modelMapper.map(updatedUser, UserResponseDTO.class));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateDTO passwordDTO) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPassword(passwordDTO.getPassword()); // Sin encriptar
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
}
