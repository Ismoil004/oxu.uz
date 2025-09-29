package com.example.backend.controller;

import com.example.backend.dto.ProblemDTO;
import com.example.backend.dto.ProblemResponseDTO;
import com.example.backend.dto.ProblemTypeDTO;
import com.example.backend.dto.RoomWithProblemsDTO;

import com.example.backend.enums.ProblemStatus;
import com.example.backend.enums.ProblemType;
import com.example.backend.service.ProblemService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProblemController {

    private final ProblemService problemService;
    @PostMapping("/room/{roomId}/all-problems")
    public ResponseEntity<List<ProblemDTO>> createAllProblemsForRoom(
            @PathVariable UUID roomId,
            @RequestParam UUID reportedById) {

        List<ProblemDTO> createdProblems = problemService.createAllProblemTypesForRoom(roomId, reportedById);
        return ResponseEntity.ok(createdProblems);
    }
    @GetMapping("/room/{roomId}/problem-types")
    public ResponseEntity<List<ProblemTypeDTO>> getProblemTypesForRoom(@PathVariable UUID roomId) {
        List<ProblemTypeDTO> problemTypes = problemService.getProblemTypesForRoom(roomId);
        return ResponseEntity.ok(problemTypes);
    }


    @PostMapping("/room/{roomId}/selected-problems")
    public ResponseEntity<List<ProblemDTO>> createSelectedProblemsForRoom(
            @PathVariable UUID roomId,
            @RequestParam UUID reportedById,
            @RequestBody ProblemRequest request) {

        List<ProblemDTO> createdProblems = problemService.createSelectedProblemsForRoom(
                roomId, reportedById, request.getProblemTypes(), request.getDescriptions());
        return ResponseEntity.ok(createdProblems);
    }

    // ✅ Yangi Request DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemRequest {
        private List<ProblemType> problemTypes;
        private List<String> descriptions; // ✅ Har bir muammo uchun alohida description
    }
    @PostMapping
    public ResponseEntity<?> createProblem(@RequestBody ProblemDTO problemDTO) {
        try {
            ProblemDTO createdProblem = problemService.createProblem(problemDTO);
            return ResponseEntity.ok(createdProblem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xatolik yuz berdi: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProblemDTO>> getAllProblems() {
        List<ProblemDTO> problems = problemService.getAllProblems();
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/room/{roomId}/problem-type/{problemType}")
    public ResponseEntity<?> getProblemByRoomAndType(
            @PathVariable UUID roomId,
            @PathVariable ProblemType problemType) {

        try {
            // Faqat mavjud muammoni olish, yangi yaratmaslik
            ProblemDTO problem = problemService.getProblemByRoomAndType(roomId, problemType);
            return ResponseEntity.ok(problem);
        } catch (RuntimeException e) {
            // Muammo topilmasa 404 qaytarish
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }



    // Muammoni statusini yangilash
// Muammoni statusini yangilash
    @PatchMapping("/{problemId}/status")
    public ResponseEntity<ProblemDTO> updateProblemStatus(
            @PathVariable UUID problemId,
            @RequestParam ProblemStatus status,
            @RequestParam(required = false) UUID resolvedById,
            @RequestParam(required = false) UUID assignedToId) { // ✅ Yangi parametr

        ProblemDTO updatedProblem = problemService.updateProblemStatus(problemId, status, resolvedById, assignedToId);
        return ResponseEntity.ok(updatedProblem);
    }
    @GetMapping("/statistics/monthly-completed")
    public ResponseEntity<List<MonthlyTechnicianStatsDTO>> getMonthlyCompletedProblemsByAllTechnicians(
            @RequestParam Integer year,
            @RequestParam Integer month) {

        List<MonthlyTechnicianStatsDTO> stats = problemService.getMonthlyCompletedProblemsByAllTechnicians(year, month);
        return ResponseEntity.ok(stats);
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTechnicianStatsDTO {
        private UUID technicianId;
        private String technicianName;
        private int year;
        private int month;
        private String monthName; // "Yanvar", "Fevral", etc.
        private long completedCount;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }
    @GetMapping("/today-completed-all")
    public ResponseEntity<List<ProblemDTO>> getAllTodayCompletedProblems() {
        List<ProblemDTO> problems = problemService.getCompletedProblemsByDate(LocalDate.now());
        return ResponseEntity.ok(problems);
    }
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ProblemDTO>> getProblemsByTechnician(@PathVariable UUID technicianId) {
        List<ProblemDTO> problems = problemService.getProblemsByTechnician(technicianId);
        return ResponseEntity.ok(problems);
    }
    @GetMapping("/statistics/user/{userId}/yearly-completed")
    public ResponseEntity<List<YearlyUserStatsDTO>> getYearlyCompletedProblemsByUser(
            @PathVariable UUID userId,
            @RequestParam Integer year) {

        List<YearlyUserStatsDTO> stats = problemService.getYearlyCompletedProblemsByUser(userId, year);
        return ResponseEntity.ok(stats);
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyUserStatsDTO {
        private UUID userId;
        private String userName;
        private int year;
        private long completedCount;
        private List<MonthlyStats> monthlyBreakdown; // ✅ Oylarga bo'lingan statistikalar

        // ✅ Oylik statistika uchun inner class
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MonthlyStats {
            private int month;
            private String monthName; // "Yanvar", "Fevral", etc.
            private long completedCount;
        }
    }
    // ProblemController.java ga yangi endpoint qo'shamiz
    @GetMapping("/technician/{technicianId}/bino-problems")
    public ResponseEntity<?> getProblemsByTechnicianBino(@PathVariable UUID technicianId) {
        try {
            List<ProblemResponseDTO> problems = problemService.getProblemsByTechnicianBino(technicianId);
            return ResponseEntity.ok(problems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/technician/{technicianId}/bino-problems/status/{status}")
    public ResponseEntity<?> getProblemsByTechnicianBinoAndStatus(
            @PathVariable UUID technicianId,
            @PathVariable String status) {
        try {
            return ResponseEntity.ok(problemService.getProblemsByTechnicianBinoAndStatus(technicianId, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/statistics/counts")
    public ResponseEntity<ProblemStatisticsDTO> getProblemCounts() {
        ProblemStatisticsDTO statistics = problemService.getProblemCounts();
        return ResponseEntity.ok(statistics);
    }

    // Yangi: Texnik bo'yicha tugallangan muammolar statistikasi
    @GetMapping("/statistics/completed-by-technician")
    public ResponseEntity<List<TechnicianCompletedStatsDTO>> getCompletedProblemsByTechnician() {
        List<TechnicianCompletedStatsDTO> stats = problemService.getCompletedProblemsByTechnician();
        return ResponseEntity.ok(stats);
    }

    // Yangi: Bugungi tugallangan muammolar
    @GetMapping("/statistics/today-completed")
    public ResponseEntity<List<ProblemDTO>> getTodayCompletedProblems() {
        List<ProblemDTO> problems = problemService.getTodayCompletedProblems();
        return ResponseEntity.ok(problems);
    }

    // DTO classes for statistics
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemStatisticsDTO {
        private long newProblemsCount;
        private long inProgressCount;
        private long completedCount;
        private long totalProblemsCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicianCompletedStatsDTO {
        private UUID technicianId;
        private String technicianName;
        private long completedCount;
    }

    // Texnikka biriktirilgan muammolarni status bo'yicha olish
    @GetMapping("/technician/{technicianId}/status/{status}")
    public ResponseEntity<List<ProblemDTO>> getProblemsByTechnicianAndStatus(
            @PathVariable UUID technicianId,
            @PathVariable ProblemStatus status) {
        List<ProblemDTO> problems = problemService.getProblemsByTechnicianAndStatus(technicianId, status);
        return ResponseEntity.ok(problems);
    }


    // Room bo'yicha muammolarni olish
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ProblemDTO>> getProblemsByRoom(@PathVariable UUID roomId) {
        List<ProblemDTO> problems = problemService.getProblemsByRoom(roomId);
        return ResponseEntity.ok(problems);
    }

    // Status bo'yicha muammolarni olish
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProblemDTO>> getProblemsByStatus(@PathVariable ProblemStatus status) {
        List<ProblemDTO> problems = problemService.getProblemsByStatus(status);
        return ResponseEntity.ok(problems);
    }


    // Floor bo'yicha muammolarni olish
    @GetMapping("/floor/{floorId}")
    public ResponseEntity<List<ProblemDTO>> getProblemsByFloor(@PathVariable UUID floorId) {
        List<ProblemDTO> problems = problemService.getProblemsByFloor(floorId);
        return ResponseEntity.ok(problems);
    }

    // Room bilan birga muammolarni olish
    @GetMapping("/room/{roomId}/with-problems")
    public ResponseEntity<RoomWithProblemsDTO> getRoomWithProblems(@PathVariable UUID roomId) {
        RoomWithProblemsDTO roomWithProblems = problemService.getRoomWithProblems(roomId);
        return ResponseEntity.ok(roomWithProblems);
    }

    // Statistikalar
    @GetMapping("/statistics")
    public ResponseEntity<ProblemService.ProblemStatistics> getProblemStatistics() {
        ProblemService.ProblemStatistics statistics = problemService.getProblemStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Active muammolarni olish (YANGI va JARAYONDA)
    @GetMapping("/active")
    public ResponseEntity<List<ProblemDTO>> getActiveProblems() {
        List<ProblemDTO> newProblems = problemService.getProblemsByStatus(ProblemStatus.YANGI);
        List<ProblemDTO> inProgressProblems = problemService.getProblemsByStatus(ProblemStatus.JARAYONDA);

        newProblems.addAll(inProgressProblems);
        return ResponseEntity.ok(newProblems);
    }
}