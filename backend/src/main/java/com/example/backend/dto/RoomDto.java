package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class RoomDto {
    private UUID roomId;
    private String roomNumber;

    public RoomDto(UUID roomId, String roomNumber) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }

}