package com.harsh.csieventmangement.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;
    private Long createdBy;
    private Integer maxTeamSize;
    private boolean scoringLocked;
}
