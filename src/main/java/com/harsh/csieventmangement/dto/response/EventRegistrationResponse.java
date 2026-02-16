package com.harsh.csieventmangement.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationResponse {

    private Long eventId;
    private Long userId;
    private LocalDateTime registeredAt;
    private String message;
}
