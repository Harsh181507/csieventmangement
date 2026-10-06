package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.*;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;

    // ✅ Assign Judge To Event
    @Transactional
    public String assignJudgeToEvent(Long eventId, Long judgeId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        User judge = userRepository.findById(judgeId)
                .orElseThrow(() ->
                        new ApiException("Judge not found", HttpStatus.NOT_FOUND));

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("User is not a JUDGE", HttpStatus.BAD_REQUEST);
        }

        if (eventJudgeRepository.findByEventAndJudge(event, judge).isPresent()) {
            throw new ApiException("Judge already assigned to this event",
                    HttpStatus.CONFLICT);
        }

        EventJudge eventJudge = EventJudge.builder()
                .event(event)
                .judge(judge)
                .build();

        eventJudgeRepository.save(eventJudge);

        return "Judge assigned to event successfully";
    }

    // ✅ Assign Judge To Team
    @Transactional
    public String assignJudgeToTeam(Long teamId, Long judgeId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        User judge = userRepository.findById(judgeId)
                .orElseThrow(() ->
                        new ApiException("Judge not found", HttpStatus.NOT_FOUND));

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("User is not a JUDGE", HttpStatus.BAD_REQUEST);
        }

        // 🔒 Judge must already be assigned to event
        if (eventJudgeRepository
                .findByEventAndJudge(team.getEvent(), judge)
                .isEmpty()) {

            throw new ApiException(
                    "Judge must be assigned to event first",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (judgeAssignmentRepository
                .findByTeamAndJudge(team, judge)
                .isPresent()) {

            throw new ApiException(
                    "Judge already assigned to this team",
                    HttpStatus.CONFLICT
            );
        }

        JudgeAssignment assignment = JudgeAssignment.builder()
                .event(team.getEvent())
                .team(team)
                .judge(judge)
                .build();

        judgeAssignmentRepository.save(assignment);

        return "Judge assigned to team successfully";
    }
}
