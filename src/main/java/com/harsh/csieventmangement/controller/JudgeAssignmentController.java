package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.AssignJudgeRequest;
import com.harsh.csieventmangement.service.JudgeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judge-assignments")
@RequiredArgsConstructor
public class JudgeAssignmentController {

    private final JudgeAssignmentService service;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> assignJudge(
            @Valid @RequestBody AssignJudgeRequest request
    ) {
        return ResponseEntity.ok(service.assignJudge(request));
    }
}
