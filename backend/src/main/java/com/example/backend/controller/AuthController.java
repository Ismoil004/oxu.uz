package com.example.backend.controller;

import com.example.backend.dto.AuthDto;
import com.example.backend.dto.RegisterDto;
import com.example.backend.entity.Users;
import com.example.backend.enums.Status;
import com.example.backend.repo.UserRepo;
import com.example.backend.service.AuthService;
import com.example.backend.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepo userRepo;

    @PostMapping("/login")
    public HttpEntity<?> loginUser(@Valid @RequestBody AuthDto dto) {
        return authService.login(dto.getUsername(), dto.getPassword());
    }

    @PostMapping("/register")
    public HttpEntity<?> registerUser(@Valid @RequestBody RegisterDto dto) {
        return authService.register(dto);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractTokenFromHeader(authorizationHeader);
            String username = jwtService.extractUsername(token);

            Users user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/pending-users")
    public ResponseEntity<List<Users>> getPendingUsers() {
        List<Users> pendingUsers = userRepo.findByStatus(String.valueOf(Status.PENDING));
        return ResponseEntity.ok(pendingUsers);
    }

    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<?> approveUser(@PathVariable UUID userId) {
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setStatus(String.valueOf(Status.ACTIVE));
            userRepo.save(user);

            return ResponseEntity.ok("User approved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error approving user: " + e.getMessage());
        }
    }
    @GetMapping("/technicians/active/count")
    public ResponseEntity<Map<String, Object>> getActiveTechniciansCount() {
        long count = userRepo.countActiveUsersByDepartment();

        Map<String, Object> response = new HashMap<>();
        response.put("department", "texnik");
        response.put("status", "ACTIVE");
        response.put("count", count);
        response.put("message", "Texnik departmentidagi ACTIVE foydalanuvchilar soni: " + count);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable UUID userId) {
        try {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setStatus(String.valueOf(Status.REJECTED));
            userRepo.save(user);

            return ResponseEntity.ok("User rejected successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting user: " + e.getMessage());
        }
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }
}