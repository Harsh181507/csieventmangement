package com.harsh.csieventmangement.entity;

import com.harsh.csieventmangement.util.EventRoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_role_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id", "role_type"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Role type
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private EventRoleType roleType;
}
