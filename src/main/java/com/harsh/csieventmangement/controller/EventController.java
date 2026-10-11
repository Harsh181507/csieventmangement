package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.CreateEventRequest;
import com.harsh.csieventmangement.dto.request.UpdateEventRequest;
import com.harsh.csieventmangement.dto.response.EventResponse;
import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
import com.harsh.csieventmangement.service.EventService;
import com.harsh.csieventmangement.service.ResultsExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ResultsExportService resultsExportService;

    
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

    // ✏️ Edit an event's details (Only ORGANIZER)
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    // 📄 Full results as a CSV file, for spreadsheets / certificates (Only ORGANIZER)
    @GetMapping("/{eventId}/results.csv")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<byte[]> exportResults(
            @PathVariable Long eventId
    ) {
        ResultsExportService.CsvFile file = resultsExportService.exportResults(eventId);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .body(file.content().getBytes(StandardCharsets.UTF_8));
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
