package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 255, message = "Event title cannot exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date cannot be in the past")
    private LocalDate eventDate;

    @NotNull(message = "Max team size is required")
    @Min(value = 1, message = "Max team size must be at least 1")
    @Max(value = 50, message = "Max team size cannot exceed 50")
    private Integer maxTeamSize;

}
