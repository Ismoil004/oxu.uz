package com.example.backend.dto;


import com.example.backend.entity.Bino;
import com.example.backend.enums.ProblemStatus;
import com.example.backend.enums.ProblemType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProblemDTO {
    private UUID uuid;
    private ProblemType problemType;
    private String description;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private Bino bino;
    private ProblemStatus status;
    private UUID roomId;
    private String roomNumber;
    private UUID reportedById;
    private String reportedByName;
    private UUID resolvedById;
    private String resolvedByName;
    private UUID assignedToId;
    private String assignedToName;
}