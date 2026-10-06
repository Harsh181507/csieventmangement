package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.EventRegistration;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    @Modifying
    @Query("DELETE FROM EventRegistration r WHERE r.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM EventRegistration r WHERE r.user = :user")
    void deleteByUser(@Param("user") User user);
}
