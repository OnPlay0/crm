package com.gestor.users.util;


import com.gestor.users.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String privateKey;

    @Value("${jwt.user.generator}")
    private String userGenerator;

    @Value("${jwt.expiration}")
    private int jwtExpiration;


    public String createToken(Authentication authentication) {
        UserDetails mainUser = (UserDetails) authentication.getPrincipal();
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(privateKey));

        User user = (User) mainUser;

        int expiration = user.getUsername().equalsIgnoreCase("invitado")
                ? 300
                : jwtExpiration;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(userGenerator)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                .claim("username", user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().getName().name())
                .signWith(key)
                .compact();
    }




    public Boolean validateToken (String token, UserDetails userDetails){
        final String userName=extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    public Boolean isTokenExpired (String token) {
            return extractExpiration(token).before(new Date());
    }



    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(privateKey));

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }


    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }


    public String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }


}
