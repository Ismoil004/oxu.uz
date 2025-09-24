package com.example.backend.service;

import com.example.backend.entity.Users;
import org.springframework.stereotype.Service;

public interface JwtService {
    String generateJwtToken(Users users);

    String extractSubject(String token);

    String generateRefreshToken(Users user);

    String extractUsername(String token);
}
