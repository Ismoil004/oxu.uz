package com.example.backend.controller;

import com.example.backend.dto.BinoDto;
import com.example.backend.dto.FloorDto;
import com.example.backend.dto.RoomDto;
import com.example.backend.service.BinoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bino")
@RequiredArgsConstructor
public class BinoController {
    private final BinoService binoService;

    // Barcha binolarni olish
    @GetMapping
    public List<BinoDto> getAllBino() {
        return binoService.getAllBino();
    }

    // Binoni ID bo'yicha olish (floors bilan)
    @GetMapping("/{binoId}")
    public BinoDto getBinoById(@PathVariable UUID binoId) {
        return binoService.getBinoById(binoId);
    }

    // Binodagi ma'lum floorni olish (rooms bilan)
    @GetMapping("/{binoId}/floor/{floorId}")
    public FloorDto getFloorById(@PathVariable UUID binoId,
                                 @PathVariable UUID floorId) {
        return binoService.getFloorById(binoId, floorId);
    }

    // Floordagi roomlarni olish
    @GetMapping("/{binoId}/floor/{floorId}/rooms")
    public List<RoomDto> getRoomsByFloorId(@PathVariable UUID binoId,
                                           @PathVariable UUID floorId) {
        return binoService.getRoomsByFloorId(binoId, floorId);
    }
}