package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.EventJudge;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventJudgeRepository extends JpaRepository<EventJudge, Long> {

    Optional<EventJudge> findByEventAndJudge(Event event, User judge);

    boolean existsByEventAndJudge(Event event, User judge);

    // Events a judge is assigned to, newest first, event loaded in the same query
    @Query("""
        SELECT ej FROM EventJudge ej
        JOIN FETCH ej.event e
        WHERE ej.judge = :judge
        ORDER BY e.eventDate DESC, e.id DESC
    """)
    List<EventJudge> findByJudgeWithEvent(@Param("judge") User judge);

    // Judges of an event with the judge user loaded in the same query
    @Query("""
        SELECT ej FROM EventJudge ej
        JOIN FETCH ej.judge j
        WHERE ej.event.id = :eventId
        ORDER BY j.name
    """)
    List<EventJudge> findByEventIdWithJudge(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM EventJudge ej WHERE ej.event.id = :eventId AND ej.judge.id = :judgeId")
    void deleteByEventIdAndJudgeId(@Param("eventId") Long eventId, @Param("judgeId") Long judgeId);

    @Modifying
    @Query("DELETE FROM EventJudge ej WHERE ej.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM EventJudge ej WHERE ej.judge = :judge")
    void deleteByJudge(@Param("judge") User judge);
}
