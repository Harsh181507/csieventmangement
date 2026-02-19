package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.SubmitScoreRequest;
import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.dto.response.ScoreResponse;
import com.harsh.csieventmangement.entity.*;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.harsh.csieventmangement.repository.JudgeAssignmentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final TeamRepository teamRepository;
    private final JudgingCriteriaRepository criteriaRepository;
    private final UserRepository userRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventJudgeRepository eventJudgeRepository;

    @Transactional
    public String submitScore(SubmitScoreRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.JUDGE) {
            throw new ApiException("Only JUDGE can submit scores",
                    HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() ->
                        new ApiException("Team not found",
                                HttpStatus.NOT_FOUND));

        // Judge assigned to team?
        if (judgeAssignmentRepository
                .findByTeamAndJudge(team, currentUser)
                .isEmpty()) {

            throw new ApiException(
                    "Judge not assigned to this team",
                    HttpStatus.FORBIDDEN
            );
        }

        // Judge assigned to event?
        if (eventJudgeRepository
                .findByEventAndJudge(team.getEvent(), currentUser)
                .isEmpty()) {

            throw new ApiException(
                    "Judge not assigned to this event",
                    HttpStatus.FORBIDDEN
            );
        }

        JudgingCriteria criteria =
                criteriaRepository.findById(request.getCriteriaId())
                        .orElseThrow(() ->
                                new ApiException("Criteria not found",
                                        HttpStatus.NOT_FOUND));

        if (!team.getEvent().getId()
                .equals(criteria.getEvent().getId())) {

            throw new ApiException(
                    "Team and criteria belong to different events",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (criteria.getEvent().isScoringLocked()) {
            throw new ApiException(
                    "Scoring is locked for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getScoreValue() > criteria.getMaxScore()) {
            throw new ApiException(
                    "Score exceeds max allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 🔥 UPSERT LOGIC (update if exists)
        Score score = scoreRepository
                .findByTeamAndJudgeAndCriteria(
                        team, currentUser, criteria)
                .orElse(null);

        if (score != null) {
            score.setScoreValue(request.getScoreValue());
        } else {
            score = Score.builder()
                    .team(team)
                    .judge(currentUser)
                    .criteria(criteria)
                    .scoreValue(request.getScoreValue())
                    .build();
        }

        scoreRepository.save(score);

        return "Score saved successfully";
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
    public List<LeaderboardResponse> getLeaderboard(Long eventId) {

        List<Object[]> results = scoreRepository.calculateLeaderboard(eventId);

        return results.stream()
                .map(obj -> LeaderboardResponse.builder()
                        .teamId((Long) obj[0])
                        .teamName((String) obj[1])
                        .totalScore((Long) obj[2])
                        .build()
                )
                .collect(Collectors.toList());
    }
    public List<ScoreResponse> getScoresByJudge() {

        User judge = getCurrentUser();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException(
                    "Only JUDGE can view their scores",
                    HttpStatus.FORBIDDEN
            );
        }

        return scoreRepository.findByJudge(judge)
                .stream()
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




}