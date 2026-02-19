package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventJudgeRepository;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.security.CustomUserDetails;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final EventJudgeRepository eventJudgeRepository;

    // ✅ CREATE EVENT
    public EventResponse createEvent(CreateEventRequest request) {

        User user = getCurrentUser();

        if (user.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can create events", HttpStatus.FORBIDDEN);
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .createdBy(user)
                .maxTeamSize(request.getMaxTeamSize())
                .build();

        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }


    // ✅ GET ALL EVENTS
    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ✅ LOCK SCORING
    public String lockScoring(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        event.setScoringLocked(true);
        eventRepository.save(event);

        return "Scoring locked successfully";
    }

    // ✅ UPDATE MAX TEAM SIZE
    public String updateMaxTeamSize(Long eventId, Integer newMaxSize) {

        if (newMaxSize == null || newMaxSize <= 0) {
            throw new ApiException("Max team size must be greater than 0", HttpStatus.BAD_REQUEST);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        List<Team> teams = teamRepository.findAll()
                .stream()
                .filter(t -> t.getEvent().getId().equals(eventId))
                .toList();

        for (Team team : teams) {
            if (team.getMembers().size() > newMaxSize) {
                throw new ApiException(
                        "Cannot reduce max team size below existing team member count",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        event.setMaxTeamSize(newMaxSize);
        eventRepository.save(event);

        return "Max team size updated successfully";
    }

    // ✅ GET CURRENT USER
    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        throw new ApiException(
                "Invalid authentication",
                HttpStatus.UNAUTHORIZED
        );
    }


    private EventResponse mapToResponse(Event event) {

        EventResponse response = new EventResponse();

        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setEventDate(event.getEventDate());
        response.setMaxTeamSize(event.getMaxTeamSize());
        response.setScoringLocked(event.isScoringLocked());

        return response;
    }

    public List<JudgeEventResponse> getJudgeEvents() {

        User judge = getCurrentUser();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("Only JUDGE can access", HttpStatus.FORBIDDEN);
        }

        return eventJudgeRepository.findByJudge(judge)
                .stream()
                .map(ej -> JudgeEventResponse.builder()
                        .id(ej.getEvent().getId())
                        .title(ej.getEvent().getTitle())
                        .description(ej.getEvent().getDescription())
                        .scoringLocked(ej.getEvent().isScoringLocked())
                        .build()
                )
                .toList();
    }




}
