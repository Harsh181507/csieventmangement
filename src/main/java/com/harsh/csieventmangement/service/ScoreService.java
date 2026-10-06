package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.BatchScoreRequest;
import com.harsh.csieventmangement.dto.request.SubmitScoreRequest;
import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.dto.response.ScoreResponse;
import com.harsh.csieventmangement.entity.*;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final TeamRepository teamRepository;
    private final JudgingCriteriaRepository criteriaRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final LeaderboardService leaderboardService;

    @Transactional
    public String submitScore(SubmitScoreRequest request) {

        BatchScoreRequest.Entry entry = new BatchScoreRequest.Entry();
        entry.setCriteriaId(request.getCriteriaId());
        entry.setScoreValue(request.getScoreValue());

        BatchScoreRequest batch = new BatchScoreRequest();
        batch.setTeamId(request.getTeamId());
        batch.setScores(List.of(entry));

        submitScores(batch);

        return "Score saved successfully";
    }

    /**
     * Saves all of a judge's scores for one team in one transaction: either
     * every score is saved or none is. Existing scores are updated (upsert).
     */
    @Transactional
    public String submitScores(BatchScoreRequest request) {

        User judge = CurrentUser.get();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("Only JUDGE can submit scores",
                    HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() ->
                        new ApiException("Team not found",
                                HttpStatus.NOT_FOUND));

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Scoring is locked for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        requireCanScore(judge, team, event);

        // Load every criterion of the event once
        Map<Long, JudgingCriteria> criteriaById = criteriaRepository
                .findByEventIdOrderByIdAsc(event.getId())
                .stream()
                .collect(Collectors.toMap(JudgingCriteria::getId, Function.identity()));

        Map<Long, Score> existing = scoreRepository.findByTeamAndJudge(team, judge)
                .stream()
                .collect(Collectors.toMap(s -> s.getCriteria().getId(), Function.identity()));

        List<Score> toSave = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (BatchScoreRequest.Entry entry : request.getScores()) {

            if (!seen.add(entry.getCriteriaId())) {
                continue; // ignore duplicates in the same request
            }

            JudgingCriteria criteria = criteriaById.get(entry.getCriteriaId());
            if (criteria == null) {
                throw new ApiException(
                        "Criteria not found for this event",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (entry.getScoreValue() > criteria.getMaxScore()) {
                throw new ApiException(
                        "Score for '" + criteria.getTitle() + "' exceeds max of " + criteria.getMaxScore(),
                        HttpStatus.BAD_REQUEST
                );
            }

            // 🔥 UPSERT LOGIC (update if exists)
            Score score = existing.get(criteria.getId());
            if (score != null) {
                score.setScoreValue(entry.getScoreValue());
            } else {
                score = Score.builder()
                        .team(team)
                        .judge(judge)
                        .criteria(criteria)
                        .scoreValue(entry.getScoreValue())
                        .build();
            }
            toSave.add(score);
        }

        scoreRepository.saveAll(toSave);

        return toSave.size() == 1 ? "Score saved successfully" : "Scores saved successfully";
    }

    /** Live standings for organizers and judges (no lock required). */
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard(Long eventId) {
        return leaderboardService.buildLeaderboard(eventId);
    }

    /**
     * Scores the current judge has given, optionally limited to one event.
     */
    @Transactional(readOnly = true)
    public List<ScoreResponse> getScoresByJudge(Long eventId) {

        User judge = CurrentUser.get();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException(
                    "Only JUDGE can view their scores",
                    HttpStatus.FORBIDDEN
            );
        }

        List<Score> scores = eventId == null
                ? scoreRepository.findByJudgeWithDetails(judge)
                : scoreRepository.findByJudgeAndEventWithDetails(judge, eventId);

        return scores.stream()
                .map(score -> ScoreResponse.builder()
                        .scoreId(score.getId())
                        .teamId(score.getTeam().getId())
                        .teamName(score.getTeam().getTeamName())
                        .criteriaId(score.getCriteria().getId())
                        .criteriaTitle(score.getCriteria().getTitle())
                        .scoreValue(score.getScoreValue())
                        .build()
                )
                .toList();
    }

    /**
     * A judge may score a team when they are assigned to its event, and
     * either they were limited to specific teams that include this one, or
     * they were not limited at all.
     */
    private void requireCanScore(User judge, Team team, Event event) {

        if (!eventJudgeRepository.existsByEventAndJudge(event, judge)) {
            throw new ApiException(
                    "Judge not assigned to this event",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean limitedToTeams = judgeAssignmentRepository.existsByJudgeAndEvent(judge, event);

        if (limitedToTeams && !judgeAssignmentRepository.existsByTeamAndJudge(team, judge)) {
            throw new ApiException(
                    "Judge not assigned to this team",
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
