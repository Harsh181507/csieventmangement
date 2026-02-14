package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.JudgingCriteria;
import com.harsh.csieventmangement.entity.Score;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Get all scores of a team
    List<Score> findByTeam(Team team);

    // Get all scores given by a judge
    List<Score> findByJudge(User judge);

    // 🔥 Leaderboard aggregation query
    @Query("""
        SELECT s.team.id, s.team.teamName, SUM(s.scoreValue)
        FROM Score s
        WHERE s.team.event.id = :eventId
        GROUP BY s.team.id, s.team.teamName
        ORDER BY SUM(s.scoreValue) DESC
    """)
    List<Object[]> calculateLeaderboard(@Param("eventId") Long eventId);
}
