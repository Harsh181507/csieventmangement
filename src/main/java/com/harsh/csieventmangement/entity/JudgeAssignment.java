package com.harsh.csieventmangement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "judge_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Event
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // 🔹 Team
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 🔹 Judge (User with role JUDGE)
    @ManyToOne
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;
}
