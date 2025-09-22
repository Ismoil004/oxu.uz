package com.example.backend.config;


import com.example.backend.entity.Users;
import com.example.backend.repo.UserRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.UUID;

@Component
public class MyFilter extends OncePerRequestFilter {
    @Autowired
    UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");
            if (token != null) {

                Jws<Claims> claims = Jwts.parser()
                        .verifyWith(signWithKey())
                        .build()
                        .parseSignedClaims(token);
                System.out.println(claims.getBody().getSubject());
                String id = claims.getPayload().getSubject();
                Users user = userRepo.findById(UUID.fromString(id)).orElseThrow();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private SecretKey signWithKey() {
        String secretKey = "mxkB8R6OYURDIk8HiiEUpBxVLQGznXl4OjDLJaNFke8=";
        byte[] decode = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decode);
    }


}



