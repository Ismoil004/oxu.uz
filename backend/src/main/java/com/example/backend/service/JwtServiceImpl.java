package com.example.backend.service;

import com.example.backend.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private SecretKey signWithKey() {
        String secretKey = "mxkB8R6OYURDIk8HiiEUpBxVLQGznXl4OjDLJaNFke8=";
        byte[] decode = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(decode);
    }
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // subject = username
    }
    @Override
    public String generateJwtToken(Users user) {
        Map<String, String> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("id", user.getUuid().toString());

        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 12))
                .claims(claims)
                .subject(user.getUuid().toString())
                .signWith(signWithKey())
                .compact();
    }

    @Override
    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(signWithKey())
                .build()
                .parseSignedClaims(token).getPayload().get("id").toString();
    }

    @Override
    public String generateRefreshToken(Users user) {
        Map<String, String> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("id", user.getUuid().toString());


        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .claims(claims)
                .subject(user.getUuid().toString())
                .signWith(signWithKey())
                .compact();
    }
}
