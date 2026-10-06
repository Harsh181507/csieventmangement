package com.harsh.csieventmangement.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaderboardResponse {

    private Long teamId;
    private String teamName;

    /**
     * Average total score per judge (sum of all scores / number of judges who
     * scored the team), rounded to 2 decimals. Averaging keeps the ranking fair
     * when teams are scored by different numbers of judges.
     */
    private Double totalScore;

    /** Number of judges who scored this team. */
    private Long judgeCount;

    private Integer rank;
}
