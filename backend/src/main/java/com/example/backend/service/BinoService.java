package com.example.backend.service;

import com.example.backend.dto.BinoDto;
import com.example.backend.dto.FloorDto;
import com.example.backend.dto.RoomDto;

import java.util.List;
import java.util.UUID;

public interface BinoService {
    List<BinoDto> getAllBino();
    BinoDto getBinoById(UUID id);
    FloorDto getFloorById(UUID binoId, UUID floorId);
    List<RoomDto> getRoomsByFloorId(UUID binoId, UUID floorId);
}