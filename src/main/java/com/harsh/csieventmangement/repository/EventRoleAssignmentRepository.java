package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.EventRoleAssignment;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.util.EventRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("DELETE FROM EventRoleAssignment a WHERE a.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM EventRoleAssignment a WHERE a.user = :user")
    void deleteByUser(@Param("user") User user);
}
