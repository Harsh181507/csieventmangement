package com.harsh.csieventmangement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreResponse {

    private Long scoreId;
    private Long teamId;
    private String teamName;
    private Long criteriaId;
    private String criteriaTitle;
    private Integer scoreValue;
}
