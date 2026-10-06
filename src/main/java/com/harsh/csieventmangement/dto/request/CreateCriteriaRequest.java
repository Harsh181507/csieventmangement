package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCriteriaRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Criteria title is required")
    @Size(max = 255, message = "Criteria title cannot exceed 255 characters")
    private String title;

    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be at least 1")
    @Max(value = 1000, message = "Max score cannot exceed 1000")
    private Integer maxScore;
}
