package com.api.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtSecurityContextRepository securityContextRepository
    ) {
        return http
                .securityContextRepository(securityContextRepository)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/auth/login", "/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/users/**").hasRole("ADMIN")
                        .pathMatchers("/api/leads/**").hasAnyRole("USER", "ADMIN", "INVITED")
                        .pathMatchers(HttpMethod.POST, "/api/clientes/upload")
                        .hasAnyRole("ADMIN", "USER", "INVITED")
                        .pathMatchers("/api/clientes/**")
                        .hasAnyRole("USER", "ADMIN", "INVITED")
                        .pathMatchers("/api/oportunidades/**")
                        .hasAnyRole("USER", "ADMIN", "INVITED")
                        .pathMatchers("/api/servicios/**")
                        .hasAnyRole("USER", "ADMIN", "INVITED")
                        .anyExchange().authenticated()
                )
                .build();
    }

}

