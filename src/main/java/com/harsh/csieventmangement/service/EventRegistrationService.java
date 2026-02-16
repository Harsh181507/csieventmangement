package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.EventRegistrationResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.EventRegistration;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRegistrationRepository;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventRegistrationResponse registerForEvent(
            Long eventId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        // 🔒 Only STUDENT can register
        if (user.getRole() != Role.STUDENT) {
            throw new ApiException(
                    "Only students can register for events",
                    HttpStatus.FORBIDDEN
            );
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(
                        "Event not found",
                        HttpStatus.NOT_FOUND
                ));

        // ❌ Duplicate check
        if (registrationRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new ApiException(
                    "You are already registered for this event",
                    HttpStatus.CONFLICT
            );
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .registeredAt(LocalDateTime.now())
                .build();

        registrationRepository.save(registration);

        return EventRegistrationResponse.builder()
                .eventId(event.getId())
                .userId(user.getId())
                .registeredAt(registration.getRegisteredAt())
                .message("Successfully registered for event")
                .build();
    }
}
