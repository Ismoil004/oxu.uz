package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherRegisterDto {
    @NotBlank(message = "Ism bo'sh bo'lmasligi kerak")
    private String firstName;

    @NotBlank(message = "Familiya bo'sh bo'lmasligi kerak")
    private String lastName;

    @NotBlank(message = "Bo'lim bo'sh bo'lmasligi kerak")
    private String department;

    @NotBlank(message = "Lavozim bo'sh bo'lmasligi kerak")
    private String position;

    @NotBlank(message = "Telefon raqam bo'sh bo'lmasligi kerak")
    private String phoneNumber;
}