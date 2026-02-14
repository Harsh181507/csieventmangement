package com.harsh.csieventmangement.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderboardResponse {

    private Long teamId;
    private String teamName;
    private Integer totalScore;
    private Integer rank;
}
