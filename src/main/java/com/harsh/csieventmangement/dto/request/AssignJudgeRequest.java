package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AssignJudgeRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private Long judgeId;

    // Optional batch assignment
    private List<Long> teamIds;
}