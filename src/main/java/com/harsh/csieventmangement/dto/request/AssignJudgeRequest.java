package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignJudgeRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private Long judgeId;

    // optional (if assigning to team)
    private Long teamId;
}
