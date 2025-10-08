package com.example.backend.service;

import com.example.backend.controller.ProblemController;
import com.example.backend.dto.ProblemDTO;
import com.example.backend.dto.ProblemResponseDTO;
import com.example.backend.dto.ProblemTypeDTO;
import com.example.backend.dto.RoomWithProblemsDTO;
import com.example.backend.entity.*;
import com.example.backend.enums.ProblemStatus;
import com.example.backend.enums.ProblemType;
import com.example.backend.repo.ProblemRepository;
import com.example.backend.repo.RoomRepository;
import com.example.backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final RoomRepository roomRepository;
    private final UserRepo userRepository;

    @Transactional // Faqat ma'lumot yozadigan methodlarda
    public ProblemDTO createProblem(ProblemDTO problemDTO) {
        Room room = roomRepository.findById(problemDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room topilmadi: " + problemDTO.getRoomId()));

        Users reportedBy = userRepository.findById(problemDTO.getReportedById())
                .orElseThrow(() -> new RuntimeException("User topilmadi: " + problemDTO.getReportedById()));

        Problem problem = new Problem();
        problem.setProblemType(problemDTO.getProblemType());
        problem.setDescription(problemDTO.getDescription());
        problem.setRoom(room);
        problem.setReportedBy(reportedBy);
        problem.setStatus(ProblemStatus.YANGI);
        problem.setReportedAt(LocalDateTime.now());

        Problem savedProblem = problemRepository.save(problem);

        // Manual flush qo'shish
        problemRepository.flush();

        return convertToDTO(savedProblem);
    }
    @Transactional(readOnly = true)
    public List<ProblemTypeDTO> getProblemTypesForRoom(UUID roomId) {
        // Xona mavjudligini tekshirish
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room topilmadi: " + roomId));

        List<ProblemTypeDTO> problemTypes = new ArrayList<>();

        // Har bir ProblemType uchun
        for (ProblemType problemType : ProblemType.values()) {
            // Bu muammo turi uchun mavjud muammolarni tekshirish
            boolean exists = problemRepository.existsByRoomUuidAndProblemType(roomId, problemType);

            String name = getProblemTypeName(problemType);
            String description = String.valueOf((problemType));

            problemTypes.add(new ProblemTypeDTO(problemType, name, description, exists));
        }

        return problemTypes;
    }
    @Transactional(readOnly = true)
    public ProblemDTO getProblemByRoomAndType(UUID roomId, ProblemType problemType) {
        Problem problem = problemRepository.findByRoomUuidAndProblemType(roomId, problemType)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Room %s uchun %s muammosi topilmadi", roomId, problemType)
                ));
        return convertToDTO(problem);
    }
    // ✅ PROBLEM TYPE NOMLARI
    private String getProblemTypeName(ProblemType problemType) {
        return switch (problemType) {
            case TELEVIZOR -> "Televizor";
            case KONDITSIONER -> "Konditsioner";
            case ELEKTR -> "Elektr";
            case WiFi -> "Wi-Fi";
            case BOSHQA -> "Boshqa";
        };
    }


    @Transactional
    public List<ProblemDTO> createAllProblemTypesForRoom(UUID roomId, UUID reportedById) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room topilmadi: " + roomId));

        Users reportedBy = userRepository.findById(reportedById)
                .orElseThrow(() -> new RuntimeException("User topilmadi: " + reportedById));

        List<ProblemDTO> createdProblems = new ArrayList<>();

        // Enum'dagi BARCHA muammo turlari uchun
        for (ProblemType problemType : ProblemType.values()) {
            Problem problem = new Problem();
            problem.setProblemType(problemType);
            problem.setDescription(String.valueOf(problemType));
            problem.setRoom(room);
            
            problem.setReportedBy(reportedBy);
            problem.setStatus(ProblemStatus.YANGI);
            problem.setReportedAt(LocalDateTime.now());

            Problem savedProblem = problemRepository.save(problem);
            createdProblems.add(convertToDTO(savedProblem));

            log.info("{} muammosi yaratildi: {}", problemType.name(), savedProblem.getUuid());
        }

        return createdProblems;
    }

    // Faqat tanlangan muammo turlarini yaratish
    @Transactional
    public List<ProblemDTO> createSelectedProblemsForRoom(UUID roomId, UUID reportedById, List<ProblemType> problemTypes, List<String> descriptions) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room topilmadi: " + roomId));

        Users reportedBy = userRepository.findById(reportedById)
                .orElseThrow(() -> new RuntimeException("User topilmadi: " + reportedById));

        List<ProblemDTO> createdProblems = new ArrayList<>();

        for (ProblemType problemType : problemTypes) {
            Problem problem = new Problem();
            problem.setProblemType(problemType);
            problem.setDescription(String.valueOf(problemType));
            problem.setRoom(room);
            problem.setReportedBy(reportedBy);
            problem.setStatus(ProblemStatus.YANGI);
            problem.setReportedAt(LocalDateTime.now());

            Problem savedProblem = problemRepository.save(problem);
            createdProblems.add(convertToDTO(savedProblem));
        }

        return createdProblems;
    }



    @Transactional // Faqat ma'lumot yozadigan methodlarda
    public ProblemDTO updateProblemStatus(UUID problemId, ProblemStatus status, UUID resolvedById, UUID assignedToId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Muammo topilmadi: " + problemId));

        problem.setStatus(status);

        if (resolvedById != null) {
            Users resolvedBy = userRepository.findById(resolvedById)
                    .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi: " + resolvedById));
            problem.setResolvedBy(resolvedBy);
            problem.setResolvedAt(LocalDateTime.now());
        }

        // ✅ Yangi: assignedTo ni yangilaymiz
        if (assignedToId != null) {
            Users assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi: " + assignedToId));
            problem.setAssignedTo(assignedTo);
        } else {
            problem.setAssignedTo(null); // Agar assignedToId null bo'lsa, assignedTo ni null qilamiz
        }

        Problem updatedProblem = problemRepository.save(problem);
        return convertToDTO(updatedProblem);
    }
    @Transactional
    public List<ProblemDTO> getProblemsByTechnician(UUID technicianId) {
        // Faqat shu texnikka biriktirilgan MUAMMOLARNI olish
        List<Problem> problems = problemRepository.findByAssignedToUuidOrResolvedByUuid(technicianId, technicianId);
        return problems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
@Transactional
    public ProblemController.ProblemStatisticsDTO getProblemCounts() {
        long newProblemsCount = problemRepository.countByStatus(ProblemStatus.YANGI);
        long inProgressCount = problemRepository.countByStatus(ProblemStatus.JARAYONDA);
        long completedCount = problemRepository.countByStatus(ProblemStatus.TUGALLANGAN);
        long totalProblemsCount = problemRepository.count();

        return new ProblemController.ProblemStatisticsDTO(newProblemsCount, inProgressCount, completedCount, totalProblemsCount);
    }
@Transactional
    public List<ProblemController.TechnicianCompletedStatsDTO> getCompletedProblemsByTechnician() {
        List<Object[]> results = problemRepository.findCompletedProblemsByTechnician();

        return results.stream().map(result -> {
            UUID technicianId = (UUID) result[0];
            String firstName = (String) result[1];
            String lastName = (String) result[2];
            Long count = (Long) result[3];

            return new ProblemController.TechnicianCompletedStatsDTO(
                    technicianId,
                    firstName + " " + lastName,
                    count != null ? count : 0
            );
        }).collect(Collectors.toList());
    }
    @Transactional
    public List<ProblemController.MonthlyTechnicianStatsDTO> getMonthlyCompletedProblemsByAllTechnicians(Integer year, Integer month) {
        List<Object[]> results = problemRepository.findMonthlyCompletedProblemsByAllTechnicians(year, month);

        return results.stream().map(result -> {
            ProblemController.MonthlyTechnicianStatsDTO stats = new ProblemController.MonthlyTechnicianStatsDTO();
            stats.setTechnicianId((UUID) result[0]);
            stats.setTechnicianName((String) result[1]);
            stats.setYear((Integer) result[2]);
            stats.setMonth((Integer) result[3]);
            stats.setCompletedCount((Long) result[4]);

            // Month name qo'shish
            String[] monthNames = {"Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
                    "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"};
            stats.setMonthName(monthNames[month - 1]);

            // Period dates
            LocalDate periodStart = LocalDate.of(year, month, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
            stats.setPeriodStart(periodStart);
            stats.setPeriodEnd(periodEnd);

            return stats;
        }).collect(Collectors.toList());
    }

    private String getMonthNameInUzbek(int month) {
        String[] uzbekMonths = {
                "Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
                "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"
        };
        return uzbekMonths[month - 1];
    }
    @Transactional
    public List<ProblemDTO> getTodayCompletedProblems() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);

        List<Problem> problems = problemRepository.findByStatusAndResolvedAtBetween(
                ProblemStatus.TUGALLANGAN, startOfDay, endOfDay);

        return problems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
@Transactional
    public List<ProblemDTO> getProblemsByTechnicianAndStatus(UUID technicianId, ProblemStatus status) {
        List<Problem> problems;

        if (status == ProblemStatus.YANGI) {
            problems = problemRepository.findByStatus(ProblemStatus.YANGI);
        } else if (status == ProblemStatus.JARAYONDA) {
            problems = problemRepository.findByStatusAndAssignedToUuid(ProblemStatus.JARAYONDA, technicianId);
        } else if (status == ProblemStatus.TUGALLANGAN) {
            // Tugallangan muammolar - faqat shu texnik tomonidan tugatilgan
            problems = problemRepository.findByStatusAndResolvedByUuid(ProblemStatus.TUGALLANGAN, technicianId);
        } else {
            problems = Collections.emptyList();
        }

        return problems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    // Barcha muammolarni olish
    @Transactional(readOnly = true) // Faqat o'qiydigan methodlarda
    public List<ProblemDTO> getAllProblems() {
        return problemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ID bo'yicha muammoni olish
    @Transactional(readOnly = true)
    public ProblemDTO getProblemById(UUID problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem topilmadi: " + problemId));
        return convertToDTO(problem);
    }

    // Room bo'yicha muammolarni olish
    @Transactional(readOnly = true)
    public List<ProblemDTO> getProblemsByRoom(UUID roomId) {
        return problemRepository.findByRoomUuid(roomId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Status bo'yicha muammolarni olish
    @Transactional(readOnly = true)
    public List<ProblemDTO> getProblemsByStatus(ProblemStatus status) {
        return problemRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Building bo'yicha muammolarni olish
    @Transactional(readOnly = true)
    public List<ProblemDTO> getProblemsByBuilding(UUID buildingId) {
        return problemRepository.findByBuildingId(buildingId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Floor bo'yicha muammolarni olish
    @Transactional(readOnly = true)
    public List<ProblemDTO> getProblemsByFloor(UUID floorId) {
        return problemRepository.findByFloorId(floorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Room bilan birga muammolarni olish
    @Transactional(readOnly = true)
    public RoomWithProblemsDTO getRoomWithProblems(UUID roomId) {
        Room room = roomRepository.findByIdWithProblems(roomId);

        RoomWithProblemsDTO dto = new RoomWithProblemsDTO();
        dto.setUuid(room.getUuid());
        dto.setRoomNumber(room.getRoomNumber());

        if (room.getFloor() != null) {
            dto.setFloorId(room.getFloor().getUuid());
            dto.setFloorNumber(room.getFloor().getFloorNumber().toString());

            if (room.getFloor().getBino() != null) {
                dto.setBuildingId(room.getFloor().getBino().getUuid());
                dto.setBuildingName(room.getFloor().getBino().getName());
            }
        }

        dto.setProblemCount(room.getProblemCount());
        dto.setActiveProblemCount(room.getActiveProblemCount());

        dto.setProblems(room.getProblems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    // Statistikalar
    @Transactional(readOnly = true)
    public ProblemStatistics getProblemStatistics() {
        long totalProblems = problemRepository.count();
        long newProblems = problemRepository.countByStatus(ProblemStatus.YANGI);
        long inProgressProblems = problemRepository.countByStatus(ProblemStatus.JARAYONDA);
        long completedProblems = problemRepository.countByStatus(ProblemStatus.TUGALLANGAN);

        return new ProblemStatistics(totalProblems, newProblems, inProgressProblems, completedProblems);
    }

    // DTO ga convert qilish
    // DTO ga convert qilish
    private ProblemDTO convertToDTO(Problem problem) {
        ProblemDTO dto = new ProblemDTO();
        dto.setUuid(problem.getUuid());
        dto.setProblemType(problem.getProblemType());
        dto.setDescription(problem.getDescription());
        dto.setReportedAt(problem.getReportedAt());
        dto.setResolvedAt(problem.getResolvedAt());
        dto.setStatus(problem.getStatus());

        if (problem.getRoom() != null) {
            dto.setRoomId(problem.getRoom().getUuid());
            dto.setRoomNumber(problem.getRoom().getRoomNumber());

            // ✅ Bino va qavat ma'lumotlarini qo'shish
            if (problem.getRoom().getFloor() != null) {
                dto.setFloorNumber(String.valueOf(problem.getRoom().getFloor().getFloorNumber()));

                if (problem.getRoom().getFloor().getBino() != null) {
                    dto.setBinoName(problem.getRoom().getFloor().getBino().getName());
                }
            }
        }

        if (problem.getReportedBy() != null) {
            dto.setReportedById(problem.getReportedBy().getUuid());
            dto.setReportedByName(problem.getReportedBy().getFirstName() + " " + problem.getReportedBy().getLastName());
        }

        if (problem.getResolvedBy() != null) {
            dto.setResolvedById(problem.getResolvedBy().getUuid());
            dto.setResolvedByName(problem.getResolvedBy().getFirstName() + " " + problem.getResolvedBy().getLastName());
        }

        if (problem.getAssignedTo() != null) {
            dto.setAssignedToId(problem.getAssignedTo().getUuid());
            dto.setAssignedToName(problem.getAssignedTo().getFirstName() + " " + problem.getAssignedTo().getLastName());
        }

        return dto;
    }
    @Transactional
    public List<ProblemController.YearlyUserStatsDTO> getYearlyCompletedProblemsByUser(UUID userId, Integer year) {
        // 1. Foydalanuvchi mavjudligini tekshirish
        Users technician = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi: " + userId));

        // 2. Yil bo'yicha tugallangan muammolarni olish
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Problem> completedProblems = problemRepository.findByResolvedByUuidAndStatusAndResolvedAtBetween(
                userId, ProblemStatus.TUGALLANGAN, startDate.atStartOfDay(), endDate.atStartOfDay());

        // 3. Oylik statistika yaratish
        List<ProblemController.YearlyUserStatsDTO.MonthlyStats> monthlyStats = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            long monthlyCount = completedProblems.stream()
                    .filter(problem -> problem.getResolvedAt().getMonthValue() == currentMonth)
                    .count();

            String monthName = getMonthNameUz(month);
            monthlyStats.add(new ProblemController.YearlyUserStatsDTO.MonthlyStats(month, monthName, monthlyCount));
        }

        // 4. Yillik statistika yaratish
        long yearlyCount = completedProblems.size();

        ProblemController.YearlyUserStatsDTO yearlyStats = new ProblemController.YearlyUserStatsDTO(
                userId,
                technician.getFirstName() + " " + technician.getLastName(),
                year,
                yearlyCount,
                monthlyStats
        );

        return List.of(yearlyStats);
    }

    private String getMonthNameUz(int month) {
        String[] monthNames = {
                "Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
                "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"
        };
        return monthNames[month - 1];
    }
    @Transactional
    public List<ProblemDTO> getCompletedProblemsByDate(LocalDate date) {
        log.info("{} sanasi uchun tugallangan muammolarni olish", date);

        try {
            List<Problem> completedProblems = problemRepository.findByStatusAndCompletedDate(
                    ProblemStatus.TUGALLANGAN, date);

            log.info("{} sanasi uchun {} ta tugallangan muammo topildi", date, completedProblems.size());

            return completedProblems.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("{} sanasi uchun tugallangan muammolarni olishda xatolik: {}", date, e.getMessage(), e);
            throw new RuntimeException("Tugallangan muammolarni olishda xatolik: " + e.getMessage());
        }
    }
    @Transactional(readOnly = true)
    public List<ProblemResponseDTO> getProblemsByTechnicianBino(UUID technicianId) {
        Users technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (technician.getBino() == null) {
            throw new RuntimeException("Technician has no assigned building");
        }

        // Yangi JOIN FETCH metodini ishlating
        List<Problem> problems = problemRepository.findByRoom_Floor_Bino_UuidWithDetails(technician.getBino().getUuid());

        return problems.stream()
                .map(this::convertToResponseDTO) // Yangi metod
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Problem> getProblemsByTechnicianBinoAndStatus(UUID technicianId, String status) {
        Users technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (technician.getBino() == null) {
            throw new RuntimeException("Technician has no assigned building");
        }

        ProblemStatus problemStatus;
        try {
            problemStatus = ProblemStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        return problemRepository.findByRoomFloorBinoUuidAndStatusWithDetails(
                technician.getBino().getUuid(),
                problemStatus
        );
    }
    private ProblemResponseDTO convertToResponseDTO(Problem problem) {
        ProblemResponseDTO dto = new ProblemResponseDTO();
        dto.setUuid(problem.getUuid());
        dto.setProblemType(String.valueOf(problem.getProblemType()));
        dto.setDescription(problem.getDescription());
        dto.setReportedAt(problem.getReportedAt());
        dto.setResolvedAt(problem.getResolvedAt());
        dto.setStatus(String.valueOf(problem.getStatus()));

        // Room ma'lumotlari
        if (problem.getRoom() != null) {
            dto.setRoomNumber(problem.getRoom().getRoomNumber());

            // Floor ma'lumotlari
            if (problem.getRoom().getFloor() != null) {
                dto.setFloorNumber(String.valueOf(problem.getRoom().getFloor().getFloorNumber()));

                if (problem.getRoom().getFloor().getBino() != null) {
                    dto.setBinoName(problem.getRoom().getFloor().getBino().getName());
                }
            }
        }

        // ✅ ReportedBy ma'lumotlari - TELEFON RAQAMINI HAM QO'SHAMIZ
        if (problem.getReportedBy() != null) {
            dto.setReportedById(problem.getReportedBy().getUuid());
            dto.setReportedByName(problem.getReportedBy().getFirstName() + " " + problem.getReportedBy().getLastName());

            // ✅ TELEFON RAQAMINI QO'SHISH
            dto.setPhoneNumber(problem.getReportedBy().getPhoneNumber());
        }

        // ResolvedBy ma'lumotlari
        if (problem.getResolvedBy() != null) {
            dto.setResolvedById(problem.getResolvedBy().getUuid());
            dto.setResolvedByName(problem.getResolvedBy().getFirstName() + " " + problem.getResolvedBy().getLastName());
            // ResolvedBy uchun ham telefon raqamini qo'shish mumkin
            // dto.setResolvedByPhone(problem.getResolvedBy().getPhoneNumber());
        }

        // AssignedTo ma'lumotlari
        if (problem.getAssignedTo() != null) {
            dto.setAssignedToId(problem.getAssignedTo().getUuid());
            dto.setAssignedToName(problem.getAssignedTo().getFirstName() + " " + problem.getAssignedTo().getLastName());
        }

        return dto;
    }

    // Statistikalar uchun inner class
    public static class ProblemStatistics {
        private final long totalProblems;
        private final long newProblems;
        private final long inProgressProblems;
        private final long completedProblems;

        public ProblemStatistics(long totalProblems, long newProblems, long inProgressProblems, long completedProblems) {
            this.totalProblems = totalProblems;
            this.newProblems = newProblems;
            this.inProgressProblems = inProgressProblems;
            this.completedProblems = completedProblems;
        }

        // Getters
        public long getTotalProblems() { return totalProblems; }
        public long getNewProblems() { return newProblems; }
        public long getInProgressProblems() { return inProgressProblems; }
        public long getCompletedProblems() { return completedProblems; }
    }
}