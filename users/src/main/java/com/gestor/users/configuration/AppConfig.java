package com.gestor.users.configuration;

import com.gestor.users.dto.UserResponseDTO;
import com.gestor.users.model.Role;
import com.gestor.users.model.RoleList;
import com.gestor.users.model.User;
import com.gestor.users.repository.RoleRepository;
import com.gestor.users.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
public class AppConfig {




    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(new PropertyMap<User, UserResponseDTO>() {
            @Override
            protected void configure() {
                map().setRole(source.getRole().getName());
            }
        });
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initUsers(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("📣 Ejecutando inicializador...");

            // Crear todos los roles si no existen
            for (RoleList roleList : RoleList.values()) {
                roleRepository.findByName(roleList)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName(roleList);
                            return roleRepository.save(newRole);
                        });
            }

            // Admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                Role adminRole = roleRepository.findByName(RoleList.ROLE_ADMIN).orElseThrow();
                User admin = User.builder()
                        .username("admin")
                        .email("admin@crm.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(adminRole)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Usuario admin creado");
            }

            // Invitado
            if (userRepository.findByUsername("invitado").isEmpty()) {
                Role invitedRole = roleRepository.findByName(RoleList.ROLE_INVITED).orElseThrow();
                User invitado = User.builder()
                        .username("invitado")
                        .email("invitado@crm.com")
                        .password(passwordEncoder.encode("123"))
                        .role(invitedRole)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(invitado);
                System.out.println("✅ Usuario invitado creado");
            }
        };
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
