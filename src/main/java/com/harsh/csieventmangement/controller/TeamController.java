package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
