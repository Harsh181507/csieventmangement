package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.CreateCriteriaRequest;
import com.harsh.csieventmangement.dto.response.CriteriaResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgingCriteria;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.JudgingCriteriaRepository;
import com.harsh.csieventmangement.repository.ScoreRepository;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgingCriteriaService {

    private final JudgingCriteriaRepository criteriaRepository;
    private final EventRepository eventRepository;
    private final ScoreRepository scoreRepository;

    // 🔹 Create Criteria (Only ORGANIZER)
    @Transactional
    public CriteriaResponse createCriteria(CreateCriteriaRequest request) {

        requireOrganizer("add criteria");

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND)
                );

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Scoring is locked — unlock it before changing criteria",
                    HttpStatus.BAD_REQUEST
            );
        }

        JudgingCriteria criteria = JudgingCriteria.builder()
                .title(request.getTitle().trim())
                .maxScore(request.getMaxScore())
                .event(event)
                .build();

        criteriaRepository.save(criteria);

        return mapToResponse(criteria);
    }

    // 🔹 Get Criteria By Event
    @Transactional(readOnly = true)
    public List<CriteriaResponse> getCriteriaByEvent(Long eventId) {

        return criteriaRepository.findByEventIdOrderByIdAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔹 Delete Criteria (Only ORGANIZER) — also removes scores given for it
    @Transactional
    public String deleteCriteria(Long criteriaId) {

        requireOrganizer("delete criteria");

        JudgingCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() ->
                        new ApiException("Criteria not found", HttpStatus.NOT_FOUND));

        if (criteria.getEvent().isScoringLocked()) {
            throw new ApiException(
                    "Scoring is locked — unlock it before changing criteria",
                    HttpStatus.BAD_REQUEST
            );
        }

        scoreRepository.deleteByCriteria(criteria);
        criteriaRepository.delete(criteria);

        return "Criteria deleted";
    }

    private void requireOrganizer(String action) {
        User currentUser = CurrentUser.get();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException(
                    "Only ORGANIZER can " + action,
                    HttpStatus.FORBIDDEN
            );
        }
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
