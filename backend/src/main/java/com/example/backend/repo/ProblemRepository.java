package com.example.backend.repo;

import com.example.backend.entity.Problem;

import com.example.backend.enums.ProblemStatus;
import com.example.backend.enums.ProblemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    // Room bo'yicha muammolarni olish
    List<Problem> findByRoomUuid(UUID roomUuid);

    // Room va Status bo'yicha muammolarni olish
    List<Problem> findByRoomUuidAndStatus(UUID roomUuid, ProblemStatus status);

    // Status bo'yicha muammolarni olish
    List<Problem> findByStatus(ProblemStatus status);

    // Muammo turi bo'yicha muammolarni olish
    List<Problem> findByProblemType(ProblemType problemType);

    List<Problem> findByAssignedToUuidOrResolvedByUuid(UUID assignedToId, UUID resolvedById);

    // Status va texnikka biriktirilgan muammolarni olish
    List<Problem> findByStatusAndAssignedToUuid(ProblemStatus status, UUID assignedToId);
    List<Problem> findByStatusAndResolvedByUuid(ProblemStatus status, UUID resolvedById);
    Optional<Problem> findByRoomUuidAndProblemType(UUID roomId, ProblemType problemType);
    boolean existsByRoomUuidAndProblemType(UUID roomId, ProblemType problemType);

    List<Problem> findByStatusAndProblemType(ProblemStatus status, ProblemType problemType);

    // User (reportedBy) bo'yicha muammolarni olish
    List<Problem> findByReportedByUuid(UUID userId);

    // User (assignedTo) bo'yicha muammolarni olish
    List<Problem> findByAssignedToUuid(UUID userId);

    // User (resolvedBy) bo'yicha muammolarni olish
    List<Problem> findByResolvedByUuid(UUID userId);

    // Building bo'yicha muammolarni olish
    @Query("SELECT p FROM Problem p WHERE p.room.floor.bino.uuid = :buildingId")
    List<Problem> findByBuildingId(@Param("buildingId") UUID buildingId);

    // Floor bo'yicha muammolarni olish
    @Query("SELECT p FROM Problem p WHERE p.room.floor.uuid = :floorId")
    List<Problem> findByFloorId(@Param("floorId") UUID floorId);

    // Building va Status bo'yicha muammolarni olish
    @Query("SELECT p FROM Problem p WHERE p.room.floor.bino.uuid = :buildingId AND p.status = :status")
    List<Problem> findByBuildingIdAndStatus(@Param("buildingId") UUID buildingId, @Param("status") ProblemStatus status);

    // Floor va Status bo'yicha muammolarni olish
    @Query("SELECT p FROM Problem p WHERE p.room.floor.uuid = :floorId AND p.status = :status")
    List<Problem> findByFloorIdAndStatus(@Param("floorId") UUID floorId, @Param("status") ProblemStatus status);

    // Vaqt oralig'ida yaratilgan muammolarni olish
    List<Problem> findByReportedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Vaqt oralig'ida yechilgan muammolarni olish
    List<Problem> findByResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Status va Vaqt oralig'ida yaratilgan muammolarni olish
    List<Problem> findByStatusAndReportedAtBetween(ProblemStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Muammolar sonini status bo'yicha hisoblash
    @Query("SELECT p.resolvedBy.uuid, p.resolvedBy.firstName, p.resolvedBy.lastName, COUNT(p) " +
            "FROM Problem p WHERE p.status = 'TUGALLANGAN' AND p.resolvedBy IS NOT NULL " +
            "GROUP BY p.resolvedBy.uuid, p.resolvedBy.firstName, p.resolvedBy.lastName " +
            "ORDER BY COUNT(p) DESC")
    List<Object[]> findCompletedProblemsByTechnician();

    List<Problem> findByStatusAndResolvedAtBetween(ProblemStatus status, LocalDateTime start, LocalDateTime end);
    // Room bo'yicha muammolar sonini hisoblash
    long countByRoomUuid(UUID roomUuid);

    // Room va Status bo'yicha muammolar sonini hisoblash
    long countByRoomUuidAndStatus(UUID roomUuid, ProblemStatus status);

    // User (reportedBy) bo'yicha muammolar sonini hisoblash
    long countByReportedByUuid(UUID userId);

    // User (assignedTo) bo'yicha muammolar sonini hisoblash
    long countByAssignedToUuid(UUID userId);

    // User (resolvedBy) bo'yicha muammolar sonini hisoblash
    long countByResolvedByUuid(UUID userId);

    // Muammo turi bo'yicha muammolar sonini hisoblash
    long countByProblemType(ProblemType problemType);

    // Status va Muammo turi bo'yicha muammolar sonini hisoblash
    long countByStatusAndProblemType(ProblemStatus status, ProblemType problemType);

    // Eng yangi muammolarni olish (tartiblangan)
    List<Problem> findTop10ByOrderByReportedAtDesc();

    // Status bo'yicha eng yangi muammolarni olish
    List<Problem> findTop10ByStatusOrderByReportedAtDesc(ProblemStatus status);

    // User ga assign qilingan va statusi yangi/jarayonda bo'lgan muammolar
    List<Problem> findByAssignedToUuidAndStatusIn(UUID userId, List<ProblemStatus> statuses);

    // Building bo'yicha muammolar statistikasi
    @Query("SELECT p.status, COUNT(p) FROM Problem p WHERE p.room.floor.bino.uuid = :buildingId GROUP BY p.status")
    List<Object[]> countProblemsByStatusForBuilding(@Param("buildingId") UUID buildingId);

    // Floor bo'yicha muammolar statistikasi
    @Query("SELECT p.status, COUNT(p) FROM Problem p WHERE p.room.floor.uuid = :floorId GROUP BY p.status")
    List<Object[]> countProblemsByStatusForFloor(@Param("floorId") UUID floorId);

    // O'rtacha yechilish vaqtini hisoblash

    // Muammo turlari bo'yicha statistika
    @Query("SELECT p.problemType, COUNT(p) FROM Problem p GROUP BY p.problemType")
    List<Object[]> countProblemsByType();

    // Faol muammolarni olish (YANGI va JARAYONDA)
    List<Problem> findByStatusIn(List<ProblemStatus> statuses);

    // Muammoni room va reportedBy bilan tekshirish
    boolean existsByRoomUuidAndReportedByUuidAndStatusIn(UUID roomUuid, UUID userId, List<ProblemStatus> statuses);
    long countByStatus(ProblemStatus status);


    // Muammolarni pagination bilan olish
    @Query("SELECT p FROM Problem p ORDER BY p.reportedAt DESC")
    List<Problem> findProblemsWithPagination();
    @Override
    <S extends Problem> S save(S entity);

    @Override
    void flush(); // Ma'lumotlarni darhol databasega yozish
}