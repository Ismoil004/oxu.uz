package com.example.backend.service;

import com.example.backend.dto.RegisterDto;
import com.example.backend.entity.Bino;
import com.example.backend.entity.Users;
import com.example.backend.enums.Status;
import com.example.backend.repo.BinoRepository;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final BinoRepository binoRepo;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public HttpEntity<?> login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        Users user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus().equals(Status.PENDING.name())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Foydalanuvchi tasdiqlanmagan. Administrator tasdiqlashini kuting.");
            response.put("requiresApproval", true);
            return ResponseEntity.status(403).body(response);
        }

        if (user.getStatus().equals(Status.REJECTED.name())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Foydalanuvchi rad etilgan. Qayta ro'yxatdan o'ting.");
            return ResponseEntity.status(403).body(response);
        }

        String accessToken = jwtService.generateJwtToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("user", Map.of(
                "id", user.getUuid(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "username", user.getUsername(),
                "department", user.getDepartment(),
                "position", user.getPosition(),
                "status", user.getStatus()
        ));

        return ResponseEntity.ok(response);
    }

    @Override
    public HttpEntity<?> register(RegisterDto dto) {
        String department = dto.getDepartment().toLowerCase();
        boolean isTechnicalOrATM = "texnik".equals(department) || "atm".equals(department);

        // ✅ Texnik/ATM uchun username tekshirish
        if (isTechnicalOrATM) {
            if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Texnik/ATM xodimlari uchun login talab qilinadi!");
            }

            if (userRepo.existsByUsername(dto.getUsername())) {
                return ResponseEntity.badRequest().body("Bunday login mavjud!");
            }
        }

        // ✅ Boshqa bo'limlar uchun telefon raqam tekshirish
        if (!isTechnicalOrATM) {
            if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Telefon raqam talab qilinadi!");
            }

            // ✅ Boshqa bo'limlar uchun username ni telefon raqamdan avtomatik yaratish
            String generatedUsername = dto.getPhoneNumber().trim();
            if (userRepo.existsByUsername(generatedUsername)) {
                return ResponseEntity.badRequest().body("Bu telefon raqam bilan foydalanuvchi allaqachon mavjud!");
            }
            dto.setUsername(generatedUsername);

            // ✅ Boshqa bo'limlar uchun parolni avtomatik yaratish
            String generatedPassword = "123456"; // Yoki boshqa default parol
            dto.setPassword(generatedPassword);
        }

        // Statusni avtomatik belgilash
        Status status = isTechnicalOrATM ? Status.PENDING : Status.ACTIVE;

        Bino bino = null;
        if (dto.getBinoId() != null) {
            bino = binoRepo.findById(dto.getBinoId())
                    .orElseThrow(() -> new RuntimeException("Bino not found with id: " + dto.getBinoId()));
        }

        Users user = Users.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .username(dto.getUsername())
                .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .status(status.name())
                .bino(bino)
                .build();

        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi!");
        response.put("status", status.name());
        response.put("requiresApproval", status == Status.PENDING);

        // ✅ Boshqa bo'limlar uchun TOKEN qaytarish (avtomatik login)
        if (!isTechnicalOrATM) {
            String accessToken = jwtService.generateJwtToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("user", Map.of(
                    "id", user.getUuid(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "username", user.getUsername(),
                    "department", user.getDepartment(),
                    "position", user.getPosition(),
                    "status", user.getStatus()
            ));
            response.put("autoGenerated", true);
            response.put("generatedUsername", dto.getUsername());
            response.put("generatedPassword", "123456");
            response.put("autoLogin", true);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public HttpEntity<?> checkToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.ok(false);
            }

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