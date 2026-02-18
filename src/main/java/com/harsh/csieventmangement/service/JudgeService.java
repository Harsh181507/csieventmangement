package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgeAssignment;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.JudgeAssignmentRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeService {

    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<TeamResponse> getAssignedTeams(Long eventId) {

        User judge = getCurrentUser();

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

        List<JudgeAssignment> assignments =
                judgeAssignmentRepository.findByJudgeAndEvent(judge, event);

        return assignments.stream()
                .map(assignment -> {
                    Team team = assignment.getTeam();
                    return TeamResponse.builder()
                            .id(team.getId())
                            .teamName(team.getTeamName())
                            .eventId(event.getId())
                            .build();
                })
                .toList();
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND)
                );
    }
}
