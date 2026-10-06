package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.AssignJudgeRequest;
import com.harsh.csieventmangement.dto.response.JudgeAssignmentResponse;
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

/**
 * Assigns judges to events.
 *
 * A judge assigned to an event can score every team in it, unless the
 * organizer limits them to specific teams. Each call to {@link #assignJudge}
 * sets the judge's full team selection for that event (empty = all teams).
 */
@Service
@RequiredArgsConstructor
public class JudgeAssignmentService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;

    @Transactional
    public String assignJudge(AssignJudgeRequest request) {

        requireOrganizer();

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new ApiException("Event not found",
                                HttpStatus.NOT_FOUND));

        User judge = userRepository.findById(request.getJudgeId())
                .orElseThrow(() ->
                        new ApiException("Judge not found",
                                HttpStatus.NOT_FOUND));

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("User is not a JUDGE",
                    HttpStatus.BAD_REQUEST);
        }

        // Assign judge to the event globally
        if (!eventJudgeRepository.existsByEventAndJudge(event, judge)) {
            EventJudge eventJudge = EventJudge.builder()
                    .event(event)
                    .judge(judge)
                    .build();
            eventJudgeRepository.save(eventJudge);
        }

        Set<Long> teamIds = new LinkedHashSet<>();
        if (request.getTeamIds() != null) {
            teamIds.addAll(request.getTeamIds());
        }
        if (request.getTeamId() != null) {
            teamIds.add(request.getTeamId());
        }
        teamIds.remove(null);

        // Replace this judge's previous team selection for the event
        judgeAssignmentRepository.deleteByEventIdAndJudgeId(event.getId(), judge.getId());

        if (teamIds.isEmpty()) {
            return judge.getName() + " can now score all teams in this event";
        }

        List<Team> teams = teamRepository.findAllById(teamIds);
        if (teams.size() != teamIds.size()) {
            throw new ApiException("One or more teams were not found", HttpStatus.NOT_FOUND);
        }

        for (Team team : teams) {
            if (!team.getEvent().getId().equals(event.getId())) {
                throw new ApiException(
                        "Team '" + team.getTeamName() + "' belongs to a different event",
                        HttpStatus.BAD_REQUEST
                );
            }

            judgeAssignmentRepository.save(
                    JudgeAssignment.builder()
                            .event(event)
                            .team(team)
                            .judge(judge)
                            .build()
            );
        }

        return judge.getName() + " assigned to " + teams.size()
                + (teams.size() == 1 ? " team" : " teams");
    }

    /** Judges of an event and the teams each one scores. */
    @Transactional(readOnly = true)
    public List<JudgeAssignmentResponse> getAssignments(Long eventId) {

        requireOrganizer();

        Map<Long, List<Team>> teamsByJudge = new HashMap<>();
        for (JudgeAssignment ja : judgeAssignmentRepository.findByEventIdWithTeam(eventId)) {
            teamsByJudge
                    .computeIfAbsent(ja.getJudge().getId(), id -> new ArrayList<>())
                    .add(ja.getTeam());
        }

        return eventJudgeRepository.findByEventIdWithJudge(eventId)
                .stream()
                .map(ej -> {
                    User judge = ej.getJudge();
                    List<Team> teams = teamsByJudge.getOrDefault(judge.getId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing(Team::getId))
                            .toList();

                    return JudgeAssignmentResponse.builder()
                            .judgeId(judge.getId())
                            .judgeName(judge.getName())
                            .judgeEmail(judge.getEmail())
                            .allTeams(teams.isEmpty())
                            .teamIds(teams.stream().map(Team::getId).toList())
                            .teamNames(teams.stream().map(Team::getTeamName).toList())
                            .build();
                })
                .toList();
    }

    /** Removes a judge from an event (scores they already gave are kept). */
    @Transactional
    public String removeJudge(Long eventId, Long judgeId) {

        requireOrganizer();

        judgeAssignmentRepository.deleteByEventIdAndJudgeId(eventId, judgeId);
        eventJudgeRepository.deleteByEventIdAndJudgeId(eventId, judgeId);

        return "Judge removed from event";
    }

    private void requireOrganizer() {
        if (CurrentUser.get().getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can manage judges",
                    HttpStatus.FORBIDDEN);
        }
    }
}
