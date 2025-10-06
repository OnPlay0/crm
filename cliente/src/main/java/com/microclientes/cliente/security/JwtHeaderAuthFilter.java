package com.microclientes.cliente.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role   = request.getHeader("X-Role");

        if (userId != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            String cleanRole = role.trim().toUpperCase();
            if (!cleanRole.startsWith("ROLE_")) {
                cleanRole = "ROLE_" + cleanRole;
            }

            GrantedAuthority authority = new SimpleGrantedAuthority(cleanRole);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));

            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("✅ Autenticado: userId={}, role={}", userId, cleanRole);
        } else {
            log.warn("⚠️ Filtro omitido: Headers faltantes o contexto ya autenticado. userId={}, role={}", userId, role);
        }

        filterChain.doFilter(request, response);
    }
}
