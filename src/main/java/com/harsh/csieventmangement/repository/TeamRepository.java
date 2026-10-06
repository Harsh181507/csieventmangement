package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByEventId(Long eventId);

    // Teams of an event with their leader loaded in the same query
    @Query("""
        SELECT t FROM Team t
        LEFT JOIN FETCH t.leader
        WHERE t.event.id = :eventId
        ORDER BY t.id
    """)
    List<Team> findByEventIdWithLeader(@Param("eventId") Long eventId);

    /**
     * Loads a team and locks its row until the transaction ends. Used when
     * adding members so two students joining at the same instant cannot
     * push a team past the max team size.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Team t WHERE t.id = :id")
    Optional<Team> findByIdForUpdate(@Param("id") Long id);

    List<Team> findByLeader(User leader);

    Optional<Team> findByJoinCode(String joinCode);

    boolean existsByTeamNameIgnoreCaseAndEvent(String teamName, Event event);

    boolean existsByJoinCode(String joinCode);

    @Modifying
    @Query("DELETE FROM Team t WHERE t.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
