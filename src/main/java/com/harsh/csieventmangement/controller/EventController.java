package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // 🔹 Create Event (Only ORGANIZER)
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    // 🔹 List All Events (Any authenticated user)
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }
    // 🔒 Lock Scoring (Only ORGANIZER)
    @PostMapping("/{eventId}/lock")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> lockScoring(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventService.lockScoring(eventId)
        );
    }

}
