package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.CreateCriteriaRequest;
import com.harsh.csieventmangement.dto.response.CriteriaResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgingCriteria;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.JudgingCriteriaRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgingCriteriaService {

    private final JudgingCriteriaRepository criteriaRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // 🔹 Create Criteria (Only ORGANIZER)
    public CriteriaResponse createCriteria(CreateCriteriaRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException(
                    "Only ORGANIZER can add criteria",
                    HttpStatus.FORBIDDEN
            );
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND)
                );

        JudgingCriteria criteria = JudgingCriteria.builder()
                .title(request.getTitle())
                .maxScore(request.getMaxScore())
                .event(event)
                .build();

        criteriaRepository.save(criteria);

        return mapToResponse(criteria);
    }

    // 🔹 Get Criteria By Event
    public List<CriteriaResponse> getCriteriaByEvent(Long eventId) {

        return criteriaRepository.findByEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔹 Helper: Get Logged-in User
    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND)
                );
    }

    // 🔹 Helper: Map Entity → DTO
    private CriteriaResponse mapToResponse(JudgingCriteria criteria) {

        return CriteriaResponse.builder()
                .id(criteria.getId())
                .title(criteria.getTitle())
                .maxScore(criteria.getMaxScore())
                .eventId(criteria.getEvent().getId())
                .build();
    }
}
