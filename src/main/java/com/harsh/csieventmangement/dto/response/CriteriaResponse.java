package com.harsh.csieventmangement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriteriaResponse {

    private Long id;
    private String title;
    private Integer maxScore;
    private Long eventId;
}

