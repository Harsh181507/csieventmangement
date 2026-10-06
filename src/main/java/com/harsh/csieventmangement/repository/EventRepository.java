package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // All events, newest first
    List<Event> findAllByOrderByEventDateDescIdDesc();

    // Get all events created by a specific organizer
    List<Event> findByCreatedBy(User user);

    // Keep events when their creator deletes their account
    @Modifying
    @Query("UPDATE Event e SET e.createdBy = null WHERE e.createdBy = :user")
    void clearCreator(@Param("user") User user);
}
