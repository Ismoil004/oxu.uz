package com.example.backend.dto;

import com.example.backend.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemResponseDTO {
    private UUID uuid;
    private String problemType;
    private String description;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String status;
    private String roomNumber;
    private String floorNumber;
    private String binoName;

    public static ProblemResponseDTO fromProblem(Problem problem) {
        ProblemResponseDTO dto = new ProblemResponseDTO();
        dto.setUuid(problem.getUuid());
        dto.setProblemType(problem.getProblemType().name());
        dto.setDescription(problem.getDescription());
        dto.setReportedAt(problem.getReportedAt());
        dto.setResolvedAt(problem.getResolvedAt());
        dto.setStatus(problem.getStatus().name());
        dto.setRoomNumber(problem.getRoom().getRoomNumber());
        dto.setFloorNumber(problem.getRoom().getFloor().getFloorNumber());
        dto.setBinoName(problem.getRoom().getFloor().getBino().getName());
        return dto;
    }
}