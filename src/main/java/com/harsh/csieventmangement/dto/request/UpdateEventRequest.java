package com.harsh.csieventmangement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Edits an event. Same fields as {@link CreateEventRequest}, but the date may
 * stay in the past (an organizer fixing a typo after the event); moving it to
 * another past date is rejected in EventService.
 */
@Getter
@Setter
public class UpdateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 255, message = "Event title cannot exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime eventTime;

    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    private String venue;

    @NotNull(message = "Max team size is required")
    @Min(value = 1, message = "Max team size must be at least 1")
    @Max(value = 50, message = "Max team size cannot exceed 50")
    private Integer maxTeamSize;
}
