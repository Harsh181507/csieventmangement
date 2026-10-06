package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignJudgeRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Judge ID is required")
    private Long judgeId;

    /**
     * Teams this judge should score. Null or empty means the judge can score
     * every team in the event. The list replaces any earlier team selection
     * for this judge in this event.
     */
    private List<Long> teamIds;

    // Single-team form sent by older app versions
    private Long teamId;
}
