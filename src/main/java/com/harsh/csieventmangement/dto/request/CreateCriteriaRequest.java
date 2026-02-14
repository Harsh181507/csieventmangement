package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCriteriaRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Criteria title is required")
    private String title;

    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be at least 1")
    private Integer maxScore;
}
