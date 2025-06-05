package com.gestor.users.util;

import com.gestor.users.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String privateKey;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(privateKey));
    }

    public String createToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        int expiration = user.getUsername().equalsIgnoreCase("invitado") ? 120 : jwtExpiration;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                .claim("username", user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().getName().name())
                .signWith(secretKey)
                .compact();
    }
}
