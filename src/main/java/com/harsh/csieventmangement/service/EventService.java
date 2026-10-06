package com.harsh.csieventmangement.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository      eventRepository;
    private final TeamRepository       teamRepository;
    private final TeamMemberRepository teamMemberRepository; // ← added for updateMaxTeamSize fix
    private final EventJudgeRepository eventJudgeRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final ScoreRepository scoreRepository;
    private final JudgingCriteriaRepository criteriaRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventRoleAssignmentRepository roleAssignmentRepository;

    /**
     * Every dashboard loads the full event list, and it rarely changes, so it is
     * cached briefly. Writes below clear it so changes show up immediately.
     */
    private final Cache<String, List<EventResponse>> eventsCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();

    // =========================================================================
    // CREATE EVENT
    // =========================================================================

    /**
     * Creates a new event. Only users with the ORGANIZER role can call this.
     */
    public EventResponse createEvent(CreateEventRequest request) {

        User currentUser = CurrentUser.get();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can create events", HttpStatus.FORBIDDEN);
        }

        Event event = Event.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .createdBy(currentUser)
                .maxTeamSize(request.getMaxTeamSize())
                .build();

        Event savedEvent = eventRepository.save(event);
        invalidateEventsCache();

        return mapToResponse(savedEvent);
    }

    // =========================================================================
    // GET ALL EVENTS
    // =========================================================================

    /**
     * Returns all events, newest first. Accessible by any authenticated user.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventsCache.get("all", key -> eventRepository.findAllByOrderByEventDateDescIdDesc()
                .stream()
                .map(this::mapToResponse)
                .toList());
    }

    // =========================================================================
    // LOCK / UNLOCK SCORING
    // =========================================================================

    /**
     * Locks scoring for an event so no further scores can be submitted.
     * Only the organizer can call this.
     */
    @Transactional
    public String lockScoring(Long eventId) {

        Event event = findEvent(eventId);

        event.setScoringLocked(true);
        eventRepository.save(event);
        invalidateEventsCache();

        return "Scoring locked successfully";
    }

    /** Re-opens scoring, e.g. after locking by mistake. */
    @Transactional
    public String unlockScoring(Long eventId) {

        Event event = findEvent(eventId);

        event.setScoringLocked(false);
        eventRepository.save(event);
        invalidateEventsCache();

        return "Scoring unlocked successfully";
    }

    // =========================================================================
    // DELETE EVENT
    // =========================================================================

    /**
     * Deletes an event with all its teams, criteria, judges and scores.
     */
    @Transactional
    public String deleteEvent(Long eventId) {

        Event event = findEvent(eventId);

        // Children first, so no foreign key points at a deleted row
        scoreRepository.deleteByEventId(eventId);
        judgeAssignmentRepository.deleteByEventId(eventId);
        eventJudgeRepository.deleteByEventId(eventId);
        teamMemberRepository.deleteByEventId(eventId);
        teamRepository.deleteByEventId(eventId);
        criteriaRepository.deleteByEventId(eventId);
        registrationRepository.deleteByEventId(eventId);
        roleAssignmentRepository.deleteByEventId(eventId);

        eventRepository.delete(event);
        invalidateEventsCache();

        return "Event deleted successfully";
    }


    @Transactional
    public String updateMaxTeamSize(Long eventId, Integer newMaxSize) {

        if (newMaxSize == null || newMaxSize <= 0) {
            throw new ApiException(
                    "Max team size must be greater than 0",
                    HttpStatus.BAD_REQUEST
            );
        }

        Event event = findEvent(eventId);

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
        invalidateEventsCache();

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

        User judge = CurrentUser.get();

        if (judge.getRole() != Role.JUDGE) {
            throw new ApiException("Only JUDGE can access this endpoint", HttpStatus.FORBIDDEN);
        }

        return eventJudgeRepository.findByJudgeWithEvent(judge)
                .stream()
                .map(ej -> JudgeEventResponse.builder()
                        .id(ej.getEvent().getId())
                        .title(ej.getEvent().getTitle())
                        .description(ej.getEvent().getDescription())
                        .eventDate(ej.getEvent().getEventDate())
                        .scoringLocked(ej.getEvent().isScoringLocked())
                        .build()
                )
                .toList();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Clears the events cache once the current transaction commits, so a
     * concurrent read cannot re-cache the data from before this change.
     */
    private void invalidateEventsCache() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventsCache.invalidateAll();
                }
            });
        } else {
            eventsCache.invalidateAll();
        }
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));
    }

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
}
