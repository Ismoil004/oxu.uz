package com.example.backend.controller;

import com.example.backend.dto.AuthDto;
import com.example.backend.dto.RegisterDto;
import com.example.backend.entity.Users;
import com.example.backend.repo.UserRepo;
import com.example.backend.service.AuthService;
import com.example.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
     private final UserRepo userRepo;
    private static final String JWT_SECRET = "your-256-bit-secret-your-256-bit-secret"; // This should be a base64 encoded string

    @PostMapping("/login")
    public HttpEntity<?> loginUser(@Valid @RequestBody AuthDto dto) {
        return authService.login(dto.getUsername(), dto.getPassword());
    }
    public class AuthUtils {
        public static String extractTokenFromHeader(String authorizationHeader) {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7);
            }
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
    }
    @PostMapping("/register")
    public HttpEntity<?> registerUser(@Valid @RequestBody RegisterDto dto) {
        return authService.register(dto);
    }
    @GetMapping("/token")
    public HttpEntity<?> checkToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = AuthUtils.extractTokenFromHeader(authorizationHeader);
            return authService.checkToken(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test(@RequestHeader String token) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return "Invalid token";
        }

        String username = claims.getSubject();
        return "Token is valid for user: " + username;
    }

    @GetMapping("/refresh")
    public HttpEntity<?> generateRefreshToken(@RequestHeader String Authorization) {
        return authService.refreshToken(Authorization);
    }
}
