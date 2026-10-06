package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.JudgingCriteria;
import com.harsh.csieventmangement.entity.Score;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    // Prevent duplicate scoring by same judge for same team + criteria
    Optional<Score> findByTeamAndJudgeAndCriteria(
            Team team,
            User judge,
            JudgingCriteria criteria
    );

    // All scores one judge gave one team (used by batch upsert)
    List<Score> findByTeamAndJudge(Team team, User judge);

    // Scores given by a judge, with team + criteria loaded in the same query
    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.team t
        JOIN FETCH s.criteria
        WHERE s.judge = :judge
    """)
    List<Score> findByJudgeWithDetails(@Param("judge") User judge);

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.team t
        JOIN FETCH s.criteria
        WHERE s.judge = :judge AND t.event.id = :eventId
    """)
    List<Score> findByJudgeAndEventWithDetails(
            @Param("judge") User judge,
            @Param("eventId") Long eventId
    );

    /**
     * Leaderboard aggregation: total points per team and how many judges
     * scored it. Rows are [teamId, teamName, sumOfScores, distinctJudges].
     */
    @Query("""
        SELECT t.id, t.teamName, SUM(s.scoreValue), COUNT(DISTINCT s.judge.id)
        FROM Score s
        JOIN s.team t
        WHERE t.event.id = :eventId
        GROUP BY t.id, t.teamName
    """)
    List<Object[]> calculateLeaderboard(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM Score s WHERE s.team.id IN (SELECT t.id FROM Team t WHERE t.event.id = :eventId)")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM Score s WHERE s.criteria = :criteria")
    void deleteByCriteria(@Param("criteria") JudgingCriteria criteria);

    @Modifying
    @Query("DELETE FROM Score s WHERE s.team = :team")
    void deleteByTeam(@Param("team") Team team);

    @Modifying
    @Query("DELETE FROM Score s WHERE s.judge = :judge")
    void deleteByJudge(@Param("judge") User judge);
}
