package com.example.backend.repo;



import com.example.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Users> findByStatus(String status); // Yangi method

    @Query("SELECT COUNT(u) FROM Users u WHERE u.department = 'texnik' AND u.status = 'ACTIVE'")
    long countActiveUsersByDepartment();

}

