package com.harsh.csieventmangement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {

    private Long id;
    private String teamName;
    private Long eventId;
}
