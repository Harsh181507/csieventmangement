package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    private String teamName;

    @NotNull(message = "Event ID is required")
    private Long eventId;
}
