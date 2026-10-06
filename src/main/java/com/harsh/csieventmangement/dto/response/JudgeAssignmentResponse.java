package com.harsh.csieventmangement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A judge assigned to an event and the teams they score.
 * An empty {@code teamIds} list means the judge scores every team.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeAssignmentResponse {

    private Long judgeId;
    private String judgeName;
    private String judgeEmail;
    private boolean allTeams;
    private List<Long> teamIds;
    private List<String> teamNames;
}
