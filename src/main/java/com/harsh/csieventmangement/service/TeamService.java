package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public String createTeam(Long eventId, String teamName) {

        User user = getCurrentUser();

        if (user.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can create teams", HttpStatus.FORBIDDEN);
        }


        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        if (event.isScoringLocked()) {
            throw new ApiException("Cannot create team. Scoring locked", HttpStatus.BAD_REQUEST);
        }

        Team team = Team.builder()
                .teamName(teamName)
                .event(event)
                .leader(user)
                .build();

// 🔥 Add leader as first member
        team.getMembers().add(user);

        teamRepository.save(team);


        return "Team created successfully";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public String joinTeam(Long teamId) {

        User user = getCurrentUser();

        if (user.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can join teams", HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException("Cannot join team. Scoring locked", HttpStatus.BAD_REQUEST);
        }

        // 🔥 IMPORTANT PART (TEAM SIZE CHECK)
        if (team.getMembers().size() >= event.getMaxTeamSize()) {
            throw new ApiException(
                    "Team already reached maximum allowed members",
                    HttpStatus.BAD_REQUEST
            );
        }

        team.getMembers().add(user);
        teamRepository.save(team);

        return "Joined team successfully";
    }

}
