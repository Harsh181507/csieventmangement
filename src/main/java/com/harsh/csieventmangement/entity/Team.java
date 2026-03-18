package com.harsh.csieventmangement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a team registered for a specific event.
 *
 * <p><strong>Join Code feature:</strong>
 * Each team has a unique {@code joinCode} (8 uppercase alphanumeric characters)
 * generated at creation time. The team leader shares this code with teammates
 * who enter it in the app to join the team directly — no scrolling through
 * a list of all teams needed.
 *
 * <p><strong>File:</strong>
 * {@code src/main/java/com/harsh/csieventmangement/entity/Team.java}
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    // -------------------------------------------------------------------------
    // Primary Key
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** The display name of the team. */
    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    /**
     * Unique 8-character uppercase alphanumeric code for joining this team.
     * Generated in {@link com.harsh.csieventmangement.service.TeamService}
     * at creation time using {@link java.util.UUID}.
     * Example: "A3F9B2C1"
     */
    @Column(name = "join_code", nullable = false, unique = true, length = 10)
    private String joinCode;

    // -------------------------------------------------------------------------
    // Relationships
    // -------------------------------------------------------------------------

    /**
     * The event this team belongs to.
     * Lazy-loaded to avoid N+1 queries when fetching team lists.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * The student who created and leads this team.
     * Leader cannot leave the team.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    // -------------------------------------------------------------------------
    // Audit
    // -------------------------------------------------------------------------

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}