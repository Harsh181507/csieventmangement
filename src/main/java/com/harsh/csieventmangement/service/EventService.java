package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 🔹 Create Event (Only ORGANIZER)
    public EventResponse createEvent(CreateEventRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException(
                    "Only ORGANIZER can create events",
                    HttpStatus.FORBIDDEN
            );
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .createdBy(currentUser)
                .scoringLocked(false).maxTeamSize(request.getMaxTeamSize() != null
                        ? request.getMaxTeamSize()
                        : 4
                ).build();


        eventRepository.save(event);

        return mapToResponse(event);
    }

    // 🔹 Get All Events (Any authenticated user)
    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔒 Lock Scoring (Only ORGANIZER)
    public String lockScoring(Long eventId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException(
                    "Only ORGANIZER can lock scoring",
                    HttpStatus.FORBIDDEN
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND)
                );

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Scoring already locked",
                    HttpStatus.BAD_REQUEST
            );
        }

        event.setScoringLocked(true);
        eventRepository.save(event);

        return "Scoring locked successfully";
    }

    // 🔹 Helper: Get logged-in user
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

    // 🔹 Helper: Map Event → EventResponse
    private EventResponse mapToResponse(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .createdBy(event.getCreatedBy().getId())
                .scoringLocked(event.isScoringLocked())
                .build();
    }
}
