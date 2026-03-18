package com.harsh.csieventmangement.dto.response;

import lombok.*;

import java.util.List;

/**
 * Response DTO for a team.
 *
 * <p>Returned by:
 * <ul>
 *   <li>GET /teams/event/{eventId}</li>
 *   <li>GET /teams/event/{eventId}/my</li>
 *   <li>POST /teams/{eventId}</li>
 * </ul>
 *
 * <p><strong>joinCode</strong> — the 8-character code the team leader shares
 * with teammates. Only shown to the leader in the Android UI, but included
 * in the response for all members so they can reshare if needed.
 *
 * <p><strong>File:</strong>
 * {@code src/main/java/com/harsh/csieventmangement/dto/response/TeamResponse.java}
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {

    /** Database ID of the team. */
    private Long id;

    /** Display name of the team. */
    private String teamName;

    /** The event this team belongs to. */
    private Long eventId;

    /** User ID of the team leader. */
    private Long leaderId;

    /** Display name of the team leader. */
    private String leaderName;

    /** Names of all team members. */
    private List<String> members;

    /**
     * The unique join code for this team.
     * Leaders share this with teammates who enter it to join.
     * Example: "A3F9B2C1"
     */
    private String joinCode;
}