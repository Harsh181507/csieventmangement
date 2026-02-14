package com.harsh.csieventmangement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "judging_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgingCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private String title;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;
}
