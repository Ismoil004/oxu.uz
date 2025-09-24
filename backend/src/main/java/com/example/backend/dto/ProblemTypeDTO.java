package com.example.backend.dto;

import com.example.backend.enums.ProblemType;
import lombok.Data;

@Data
public class ProblemTypeDTO {
    private ProblemType type;
    private String name;
    private String description;
    private boolean exists; // Bu xonada muammo mavjudligi

    public ProblemTypeDTO(ProblemType type, String name, String description, boolean exists) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.exists = exists;
    }
}