package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
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

    // 🔹 One event (Any authenticated user)
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    // 🔓 Unlock Scoring (Only ORGANIZER)
    @PostMapping("/{eventId}/unlock")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> unlockScoring(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventService.unlockScoring(eventId)
        );
    }

    // 🗑 Delete Event with its teams, criteria and scores (Only ORGANIZER)
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> deleteEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventService.deleteEvent(eventId)
        );
    }

    @GetMapping("/judge")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<List<JudgeEventResponse>> getJudgeEvents() {
        return ResponseEntity.ok(eventService.getJudgeEvents());
    }
}
