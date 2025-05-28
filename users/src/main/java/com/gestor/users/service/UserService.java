package com.gestor.users.service;

import com.gestor.users.excepciones.UserNotFoundException;
import com.gestor.users.model.Role;
import com.gestor.users.model.User;
import com.gestor.users.repository.RoleRepository;
import com.gestor.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getRole() == null) {
            throw new IllegalArgumentException("El usuario debe tener un rol asignado.");
        }

        // Buscar y asignar rol
        Role roleEntity = roleRepository.findByName(user.getRole().getName())
                .orElseThrow(() -> new RuntimeException("El rol " + user.getRole().getName() + " no existe"));
        user.setRole(roleEntity);

        // Encriptar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));


        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        if (userDetails.getUsername() != null) user.setUsername(userDetails.getUsername());
        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());

        if (userDetails.getRole() != null) {
            Role roleEntity = roleRepository.findByName(userDetails.getRole().getName())
                    .orElseThrow(() -> new RuntimeException("El rol " + userDetails.getRole().getName() + " no existe"));
            user.setRole(roleEntity);
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
        return user;
    }



    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

}
