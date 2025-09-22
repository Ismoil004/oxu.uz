package com.example.backend.service;

import com.example.backend.dto.RegisterDto;
import com.example.backend.entity.Users;
import com.example.backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String JWT_SECRET = "your-256-bit-secret-your-256-bit-secret";

    @Override
    public HttpEntity<?> login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        Users user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateJwtToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(Map.of("access_token", accessToken, "refresh_token", refreshToken));
    }
    @Override public HttpEntity<?> register(RegisterDto dto)
    { if (userRepo.existsByUsername(dto.getUsername()))
    { return ResponseEntity.badRequest().body("Bunday login mavjud!"); }
        Users user = Users.builder() .firstName(dto.getFirstName()) .lastName(dto.getLastName()) .department(dto.getDepartment()) .position(dto.getPosition()) .username(dto.getUsername()) .password(passwordEncoder.encode(dto.getPassword())) .build(); userRepo.save(user); return ResponseEntity.ok("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi!"); }
    @Override
    public HttpEntity<?> checkToken(String token) {
        try {
            // Validate token format first
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.ok(false);
            }

            // Check if token contains whitespace (shouldn't at this point)
            if (token.contains(" ")) {
                return ResponseEntity.ok(false);
            }

            UUID uuid = UUID.fromString(jwtService.extractSubject(token));
            Users users = userRepo.findById(uuid).orElseThrow();
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public HttpEntity<?> refreshToken(String token) {
        UUID uuid = UUID.fromString(jwtService.extractSubject(token));
        Users users = userRepo.findById(uuid).orElseThrow();
        String newAccessToken = jwtService.generateRefreshToken(users);

        return ResponseEntity.ok(Map.of("access_token", newAccessToken, "refresh_token", token));
    }



}
