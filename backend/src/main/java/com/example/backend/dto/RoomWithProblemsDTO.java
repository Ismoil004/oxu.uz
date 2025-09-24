package com.example.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class RoomWithProblemsDTO {
    private UUID uuid;
    private String roomNumber;
    private UUID floorId;
    private String floorNumber;
    private UUID buildingId;
    private String buildingName;
    private int problemCount;
    private int activeProblemCount;
    private List<ProblemDTO> problems;
}