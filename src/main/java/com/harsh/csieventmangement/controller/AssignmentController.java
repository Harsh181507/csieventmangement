package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 🔹 Assign Judge To Event (Only ORGANIZER)
    @PostMapping("/event/{eventId}/judge/{judgeId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> assignJudgeToEvent(
            @PathVariable Long eventId,
            @PathVariable Long judgeId
    ) {
        return ResponseEntity.ok(
                assignmentService.assignJudgeToEvent(eventId, judgeId)
        );
    }

    // 🔹 Assign Judge To Team (Only ORGANIZER)
    @PostMapping("/team/{teamId}/judge/{judgeId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> assignJudgeToTeam(
            @PathVariable Long teamId,
            @PathVariable Long judgeId
    ) {
        return ResponseEntity.ok(
                assignmentService.assignJudgeToTeam(teamId, judgeId)
        );
    }
}
