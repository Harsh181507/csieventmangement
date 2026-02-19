package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.SubmitScoreRequest;
import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.dto.response.ScoreResponse;
import com.harsh.csieventmangement.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    // 🔹 Submit Score (Only JUDGE)
    @PostMapping
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<String> submitScore(
            @Valid @RequestBody SubmitScoreRequest request
    ) {
        return ResponseEntity.ok(scoreService.submitScore(request));
    }


    @GetMapping("/judge")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<List<ScoreResponse>> getScoresByJudge() {
        return ResponseEntity.ok(
                scoreService.getScoresByJudge()
        );
    }
    @GetMapping("/event/{eventId}/summary")
    @PreAuthorize("hasAnyRole('ORGANIZER','JUDGE')")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                scoreService.getLeaderboard(eventId)
        );
    }



}
