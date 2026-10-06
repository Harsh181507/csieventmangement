package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgeAssignment;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventJudgeRepository;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.JudgeAssignmentRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeService {

    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    /**
     * Teams the current judge should score in an event: the teams the
     * organizer picked for them, or every team when none were picked.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getAssignedTeams(Long eventId) {

        User judge = CurrentUser.get();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException(
                    "Only JUDGE can access assigned teams",
                    HttpStatus.FORBIDDEN
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND)
                );

        if (!eventJudgeRepository.existsByEventAndJudge(event, judge)) {
            throw new ApiException(
                    "You are not assigned to judge this event",
                    HttpStatus.FORBIDDEN
            );
        }

        List<Team> teams = judgeAssignmentRepository
                .findByJudgeAndEventWithTeam(judge, event)
                .stream()
                .map(JudgeAssignment::getTeam)
                .toList();

        if (teams.isEmpty()) {
            teams = teamRepository.findByEventIdWithLeader(eventId);
        }

        return teamService.mapToResponses(teams, judge);
    }
}
