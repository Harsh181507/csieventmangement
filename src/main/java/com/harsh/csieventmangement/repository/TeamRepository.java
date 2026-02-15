package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // Get all teams of a specific event
    List<Team> findByEvent(Event event);

    // Check if leader already created a team
    Optional<Team> findByLeader(User leader);

    // Prevent duplicate team names in same event
    boolean existsByTeamNameAndEvent(String teamName, Event event);

    @Query("""
           SELECT t FROM Team t
           JOIN t.members m
           WHERE m = :user AND t.event.id = :eventId
           """)
    Optional<Team> findTeamByUserAndEvent(User user, Long eventId);
}
