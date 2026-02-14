package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitScoreRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Criteria ID is required")
    private Long criteriaId;

    @NotNull(message = "Score value is required")
    @Min(value = 0, message = "Score cannot be negative")
    private Integer scoreValue;
}
