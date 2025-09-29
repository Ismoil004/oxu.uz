package com.example.backend.dto;

import com.example.backend.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterDto {
    @NotBlank(message = "Ism bo'sh bo'lmasligi kerak")
    private String firstName;

    @NotBlank(message = "Familiya bo'sh bo'lmasligi kerak")
    private String lastName;

    @NotBlank(message = "Bo'lim bo'sh bo'lmasligi kerak")
    private String department;

    @NotBlank(message = "Lavozim bo'sh bo'lmasligi kerak")
    private String position;

    @NotBlank(message = "Login bo'sh bo'lmasligi kerak")
    @Size(min = 6, message = "Login kamida 6 ta belgidan iborat bo'lishi kerak")
    private String username;

    @NotBlank(message = "Parol bo'sh bo'lmasligi kerak")
    @Size(min = 6, message = "Parol kamida 6 ta belgidan iborat bo'lishi kerak")
    private String password;
    private Status status;
    @NotNull(message = "Bino tanlanishi shart")
    private UUID binoId;
}
