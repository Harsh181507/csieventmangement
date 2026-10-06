package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgeAssignment;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, Long> {

    Optional<JudgeAssignment> findByTeamAndJudge(Team team, User judge);

    boolean existsByTeamAndJudge(Team team, User judge);

    // True when the judge was limited to specific teams in this event
    boolean existsByJudgeAndEvent(User judge, Event event);

    @Query("""
        SELECT ja FROM JudgeAssignment ja
        JOIN FETCH ja.team t
        WHERE ja.judge = :judge AND ja.event = :event
        ORDER BY t.id
    """)
    List<JudgeAssignment> findByJudgeAndEventWithTeam(
            @Param("judge") User judge,
            @Param("event") Event event
    );

    @Query("""
        SELECT ja FROM JudgeAssignment ja
        JOIN FETCH ja.team
        WHERE ja.event.id = :eventId
    """)
    List<JudgeAssignment> findByEventIdWithTeam(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM JudgeAssignment ja WHERE ja.event.id = :eventId AND ja.judge.id = :judgeId")
    void deleteByEventIdAndJudgeId(@Param("eventId") Long eventId, @Param("judgeId") Long judgeId);

    @Modifying
    @Query("DELETE FROM JudgeAssignment ja WHERE ja.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM JudgeAssignment ja WHERE ja.team = :team")
    void deleteByTeam(@Param("team") Team team);

    @Modifying
    @Query("DELETE FROM JudgeAssignment ja WHERE ja.judge = :judge")
    void deleteByJudge(@Param("judge") User judge);
}
