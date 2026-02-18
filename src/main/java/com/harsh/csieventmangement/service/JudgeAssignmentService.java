package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.AssignJudgeRequest;
import com.harsh.csieventmangement.entity.*;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.security.CustomUserDetails;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JudgeAssignmentService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;

    public String assignJudge(AssignJudgeRequest request) {

        User organizer = getCurrentUser();

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can assign judges",
                    HttpStatus.FORBIDDEN);
        }

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

        // 🔥 Assign judge to event
        if (!eventJudgeRepository.existsByEventAndJudge(event, judge)) {

            EventJudge eventJudge = EventJudge.builder()
                    .event(event)
                    .judge(judge)
                    .build();

            eventJudgeRepository.save(eventJudge);
        }

        // 🔥 Optional team assignment
        if (request.getTeamId() != null) {

            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() ->
                            new ApiException("Team not found",
                                    HttpStatus.NOT_FOUND));

            if (!judgeAssignmentRepository.existsByTeamAndJudge(team, judge)) {

                JudgeAssignment assignment = JudgeAssignment.builder()
                        .event(event)
                        .team(team)
                        .judge(judge)
                        .build();

                judgeAssignmentRepository.save(assignment);
            }
        }

        return "Judge assigned successfully";
    }

    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }
}
