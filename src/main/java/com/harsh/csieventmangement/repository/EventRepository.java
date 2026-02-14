package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Get all events created by a specific organizer
    List<Event> findByCreatedBy(User user);

    // Get only unlocked events (useful later)
    List<Event> findByScoringLockedFalse();

    // Get locked events
    List<Event> findByScoringLockedTrue();
}
