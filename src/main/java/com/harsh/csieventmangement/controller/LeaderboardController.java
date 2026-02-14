package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{eventId}")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                leaderboardService.getLeaderboard(eventId)
        );
    }
}
