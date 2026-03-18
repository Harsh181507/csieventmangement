package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.security.CustomUserDetails;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository      eventRepository;
    private final TeamRepository       teamRepository;
    private final TeamMemberRepository teamMemberRepository; // ← added for updateMaxTeamSize fix
    private final UserRepository userRepository;
    private final EventJudgeRepository eventJudgeRepository;

    // =========================================================================
    // CREATE EVENT
    // =========================================================================

    /**
     * Creates a new event. Only users with the ORGANIZER role can call this.
     */
    public EventResponse createEvent(CreateEventRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can create events", HttpStatus.FORBIDDEN);
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .createdBy(currentUser)
                .maxTeamSize(request.getMaxTeamSize())
                .build();

        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    // =========================================================================
    // GET ALL EVENTS
    // =========================================================================

    /**
     * Returns all events. Accessible by any authenticated user.
     */
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================================
    // LOCK SCORING
    // =========================================================================

    /**
     * Locks scoring for an event so no further scores can be submitted.
     * Only the organizer can call this.
     */
    public String lockScoring(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        event.setScoringLocked(true);
        eventRepository.save(event);

        return "Scoring locked successfully";
    }


    public String updateMaxTeamSize(Long eventId, Integer newMaxSize) {

        if (newMaxSize == null || newMaxSize <= 0) {
            throw new ApiException(
                    "Max team size must be greater than 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        // FIX: was teamRepository.findAll().filter().forEach(team.getMembers().size())
        // Now use TeamMemberRepository.countByTeam() instead of the deleted members Set
        teamRepository.findByEventId(eventId).forEach(team -> {
            long memberCount = teamMemberRepository.countByTeam(team);
            if (memberCount > newMaxSize) {
                throw new ApiException(
                        "Cannot reduce max team size — team '" + team.getTeamName()
                                + "' already has " + memberCount + " members",
                        HttpStatus.BAD_REQUEST
                );
            }
        });

        event.setMaxTeamSize(newMaxSize);
        eventRepository.save(event);

        return "Max team size updated successfully";
    }

    // =========================================================================
    // GET JUDGE EVENTS
    // =========================================================================

    /**
     * Returns only the events the currently authenticated judge is assigned to.
     * Called by GET /judge/events — requires JUDGE role.
     */
    @Transactional(readOnly = true)
    public List<JudgeEventResponse> getJudgeEvents() {

        User judge = getCurrentUser();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("Only JUDGE can access this endpoint", HttpStatus.FORBIDDEN);
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

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Maps an {@link Event} entity to an {@link EventResponse} DTO.
     *
     * FIX: was not setting {@code createdBy}, so every event returned
     * {@code "createdBy": null}. Now reads the creator's ID with a null guard
     * (null guard needed because legacy test data may have no creator set).
     */
    private EventResponse mapToResponse(Event event) {

        // Null guard — some events in the DB were created before the createdBy
        // FK was enforced, so their createdBy may be null
        Long createdById = null;
        if (event.getCreatedBy() != null) {
            createdById = event.getCreatedBy().getId();
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .createdBy(createdById)
                .maxTeamSize(event.getMaxTeamSize())
                .scoringLocked(event.isScoringLocked())
                .build();
    }

    /**
     * Gets the currently authenticated user from the Spring Security context.
     */
    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        throw new ApiException("Invalid authentication", HttpStatus.UNAUTHORIZED);
    }
}