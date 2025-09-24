package com.example.backend.repo;

import com.example.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByFloorUuid(UUID floorUuid);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.problems WHERE r.uuid = :roomId")
    Room findByIdWithProblems(@Param("roomId") UUID roomId);

    @Query("SELECT r FROM Room r WHERE r.floor.uuid = :floorId AND SIZE(r.problems) > 0")
    List<Room> findRoomsWithProblemsByFloorId(@Param("floorId") UUID floorId);
}