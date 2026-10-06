package com.harsh.csieventmangement.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeEventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private boolean scoringLocked;
}
