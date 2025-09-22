// BinoDto.java
package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinoDto {
    private UUID binoId;
    private String name;
    private List<FloorDto> floors;
}