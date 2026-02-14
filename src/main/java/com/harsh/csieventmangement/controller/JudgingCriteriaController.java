package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.CreateCriteriaRequest;
import com.harsh.csieventmangement.dto.response.CriteriaResponse;
import com.harsh.csieventmangement.service.JudgingCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/criteria")
@RequiredArgsConstructor
public class JudgingCriteriaController {

    private final JudgingCriteriaService criteriaService;

    // 🔹 Add Criteria (Only ORGANIZER)
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<CriteriaResponse> createCriteria(
            @Valid @RequestBody CreateCriteriaRequest request
    ) {
        return ResponseEntity.ok(criteriaService.createCriteria(request));
    }

    // 🔹 Get Criteria By Event
    @GetMapping("/{eventId}")
    public ResponseEntity<List<CriteriaResponse>> getCriteriaByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(criteriaService.getCriteriaByEvent(eventId));
    }
}
