package com.harsh.csieventmangement.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {

    private Long id;
    private String teamName;
    private Long eventId;

    private Long leaderId;
    private String leaderName;

    private List<String> members;
}
