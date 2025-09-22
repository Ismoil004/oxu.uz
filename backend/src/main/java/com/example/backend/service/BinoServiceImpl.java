package com.example.backend.service;

import com.example.backend.dto.BinoDto;
import com.example.backend.dto.FloorDto;
import com.example.backend.dto.RoomDto;
import com.example.backend.entity.Bino;
import com.example.backend.entity.Floor;
import com.example.backend.entity.Room;
import com.example.backend.repo.BinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BinoServiceImpl implements BinoService {
    private final BinoRepository binoRepository;

    @Override
    public List<BinoDto> getAllBino() {
        return binoRepository.findAll().stream()
                .map(this::mapToBinoDto)
                .collect(Collectors.toList());
    }

    @Override
    public BinoDto getBinoById(UUID id) {
        Bino bino = binoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bino topilmadi"));
        return mapToBinoDto(bino);
    }

    @Override
    public FloorDto getFloorById(UUID binoId, UUID floorId) {
        Bino bino = binoRepository.findById(binoId)
                .orElseThrow(() -> new RuntimeException("Bino topilmadi"));

        Floor floor = bino.getFloors().stream()
                .filter(f -> f.getUuid().equals(floorId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Floor topilmadi"));

        return mapToFloorDto(floor);
    }

    @Override
    public List<RoomDto> getRoomsByFloorId(UUID binoId, UUID floorId) {
        FloorDto floorDto = getFloorById(binoId, floorId);
        return floorDto.getRooms();
    }

    private BinoDto mapToBinoDto(Bino bino) {
        return new BinoDto(
                bino.getUuid(),
                bino.getName(),
                bino.getFloors().stream()
                        .map(this::mapToFloorDto)
                        .collect(Collectors.toList())
        );
    }

    private FloorDto mapToFloorDto(Floor floor) {
        return new FloorDto(
                floor.getUuid(),
                floor.getFloorNumber(),
                floor.getRooms().stream()
                        .map(this::mapToRoomDto)
                        .collect(Collectors.toList())
        );
    }

    private RoomDto mapToRoomDto(Room room) {
        return new RoomDto(room.getUuid(),room.getRoomNumber());
    }
}