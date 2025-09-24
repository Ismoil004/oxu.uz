package com.example.backend.entity;

import com.example.backend.enums.ProblemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Problem> problems = new ArrayList<>();

    // Active muammolarni olish uchun helper method
    public List<Problem> getActiveProblems() {
        return problems.stream()
                .filter(problem -> problem.getStatus() != ProblemStatus.TUGALLANGAN)
                .toList();
    }

    // Yangi muammolarni olish uchun helper method
    public List<Problem> getNewProblems() {
        return problems.stream()
                .filter(problem -> problem.getStatus() == ProblemStatus.YANGI)
                .toList();
    }

    // Muammolar sonini olish uchun helper method
    public int getProblemCount() {
        return problems.size();
    }

    public int getActiveProblemCount() {
        return getActiveProblems().size();
    }
}
