package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.response.EventRegistrationResponse;
import com.harsh.csieventmangement.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    // 🔹 Register for event (Only STUDENT)
    @PostMapping("/{eventId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                registrationService.registerForEvent(eventId, authentication)
        );
    }
}
