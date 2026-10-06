package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.AssignJudgeRequest;
import com.harsh.csieventmangement.dto.response.JudgeAssignmentResponse;
import com.harsh.csieventmangement.service.JudgeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/judge-assignments")
@RequiredArgsConstructor
public class JudgeAssignmentController {

    private final JudgeAssignmentService service;

    // Sets which teams a judge scores in an event (no teams = all teams)
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> assignJudge(
            @Valid @RequestBody AssignJudgeRequest request
    ) {
        return ResponseEntity.ok(service.assignJudge(request));
    }

    // Judges assigned to an event and their teams
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<JudgeAssignmentResponse>> getAssignments(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(service.getAssignments(eventId));
    }

    // Removes a judge from an event
    @DeleteMapping("/event/{eventId}/judge/{judgeId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> removeJudge(
            @PathVariable Long eventId,
            @PathVariable Long judgeId
    ) {
        return ResponseEntity.ok(service.removeJudge(eventId, judgeId));
    }
}
