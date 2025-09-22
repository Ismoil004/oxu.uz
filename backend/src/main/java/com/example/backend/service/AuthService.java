package com.example.backend.service;

import com.example.backend.dto.RegisterDto;
import com.example.backend.entity.Users;
import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;

public interface AuthService {
    HttpEntity<?> login(String username, String password);
    HttpEntity<?> checkToken(String token);
    HttpEntity<?> refreshToken(String token);

    HttpEntity<?> register(@Valid RegisterDto dto);
}

