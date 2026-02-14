package com.harsh.csieventmangement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // title varchar NOT NULL
    @Column(nullable = false)
    private String title;

    // description text
    @Column(columnDefinition = "TEXT")
    private String description;

    // event_date date
    @Column(name = "event_date")
    private LocalDate eventDate;

    // created_by bigint (FK users)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // scoring_locked boolean DEFAULT false
    @Column(name = "scoring_locked")
    private boolean scoringLocked = false;

    // created_at timestamp DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
