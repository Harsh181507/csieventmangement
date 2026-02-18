package com.harsh.csieventmangement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeEventResponse {

    private Long id;
    private String title;
    private String description;
    private boolean scoringLocked;
}
