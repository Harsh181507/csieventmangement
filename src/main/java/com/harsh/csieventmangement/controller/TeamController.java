package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // 🔹 Get Teams by Event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TeamResponse>> getTeamsByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                teamService.getTeamsByEvent(eventId)
        );
    }

    // 🔹 Create Team (Only STUDENT)
    @PostMapping("/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> createTeam(
            @PathVariable Long eventId,
            @RequestParam String teamName
    ) {
        return ResponseEntity.ok(
                teamService.createTeam(eventId, teamName)
        );
    }

    // 🔹 Join Team (Only STUDENT)
    @PostMapping("/join/{teamId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> joinTeam(
            @PathVariable Long teamId
    ) {
        return ResponseEntity.ok(
                teamService.joinTeam(teamId)
        );
    }
}
