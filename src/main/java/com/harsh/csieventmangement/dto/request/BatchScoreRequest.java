package com.harsh.csieventmangement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * All of one judge's scores for one team, saved in a single request.
 */
@Getter
@Setter
public class BatchScoreRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotEmpty(message = "At least one score is required")
    @Valid
    private List<Entry> scores;

    @Getter
    @Setter
    public static class Entry {

        @NotNull(message = "Criteria ID is required")
        private Long criteriaId;

        @NotNull(message = "Score value is required")
        @Min(value = 0, message = "Score cannot be negative")
        private Integer scoreValue;
    }
}
