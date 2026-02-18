package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.response.JudgeEventResponse;
import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.service.EventService;
import com.harsh.csieventmangement.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final EventService eventService;
    private final JudgeService judgeService;

    @GetMapping("/events")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<List<JudgeEventResponse>> getMyEvents() {
        return ResponseEntity.ok(eventService.getJudgeEvents());
    }

    @GetMapping("/events/{eventId}/teams")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<List<TeamResponse>> getAssignedTeam(@PathVariable Long eventId) {
        return ResponseEntity.ok(judgeService.getAssignedTeams(eventId));
    }
}
