package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthDto {
    @NotBlank(message = "Username bo'sh bo'lmasligi kerak")
    @Size(min = 6, message = "Username kamida 6 ta belgidan iborat bo'lishi kerak")
    private String username;

    @NotBlank(message = "Password bo'sh bo'lmasligi kerak")
    @Size(min = 6, message = "Password kamida 6 ta belgidan iborat bo'lishi kerak")
    private String password;
}
