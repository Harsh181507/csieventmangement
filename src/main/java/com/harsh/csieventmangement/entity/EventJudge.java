package com.harsh.csieventmangement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_judges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventJudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // 🔹 Judge (User with role JUDGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id", nullable = false)
    private User judge;
}
