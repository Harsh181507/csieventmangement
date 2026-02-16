package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.EventRoleAssignment;
import com.harsh.csieventmangement.util.EventRoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRoleAssignmentRepository
        extends JpaRepository<EventRoleAssignment, Long> {

    List<EventRoleAssignment> findByUserId(Long userId);

    Optional<EventRoleAssignment> findByUserIdAndEventIdAndRoleType(
            Long userId,
            Long eventId,
            EventRoleType roleType
    );
}
