package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.TeamResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.TeamMemberRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for all team operations.
 *
 * <p><strong>Join Code feature:</strong>
 * When a team is created, an 8-character unique uppercase code is generated
 * using the first 8 characters of a UUID (e.g. "A3F9B2C1"). This code is
 * stored on the team and returned in the API response. The leader shares
 * this code with teammates who call POST /teams/join-by-code to join.
 *
 * <p><strong>File:</strong>
 * {@code src/main/java/com/harsh/csieventmangement/service/TeamService.java}
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository       teamRepository;
    private final EventRepository      eventRepository;
    private final UserRepository       userRepository;
    private final TeamMemberRepository teamMemberRepository;

    // =========================================================================
    // CREATE TEAM
    // =========================================================================

    /**
     * Creates a new team for the given event.
     * The calling student is set as leader and added as the first member.
     * A unique 8-character join code is generated automatically.
     */
    @Transactional
    public TeamResponse createTeam(Long eventId, String teamName) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can create teams", HttpStatus.FORBIDDEN);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Cannot create team — scoring is locked",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (teamMemberRepository.existsByUserAndTeam_Event(currentUser, event)) {
            throw new ApiException(
                    "You are already in a team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Generate a unique 8-character uppercase join code
        String joinCode = generateUniqueJoinCode();

        Team team = Team.builder()
                .teamName(teamName)
                .event(event)
                .leader(currentUser)
                .joinCode(joinCode)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add creator as first member
        teamMemberRepository.save(
                TeamMember.builder()
                        .team(savedTeam)
                        .user(currentUser)
                        .build()
        );

        return mapToResponse(savedTeam);
    }

    // =========================================================================
    // JOIN TEAM BY CODE
    // =========================================================================

    /**
     * Adds the current student to a team using the team's join code.
     *
     * <p>This is the primary way teammates join a team. The leader shares
     * the code (shown on their team card) via WhatsApp or verbally, and
     * teammates enter it here.
     *
     * @param code the join code (case-insensitive, e.g. "a3f9b2c1" or "A3F9B2C1")
     * @return a response containing the team the student just joined
     */
    @Transactional
    public TeamResponse joinTeamByCode(String code) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can join teams", HttpStatus.FORBIDDEN);
        }

        // Uppercase the input so codes are case-insensitive
        Team team = teamRepository.findByJoinCode(code.toUpperCase().trim())
                .orElseThrow(() ->
                        new ApiException(
                                "Invalid join code — double check and try again",
                                HttpStatus.NOT_FOUND
                        ));

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Cannot join team — scoring is locked for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Guard: student already in a team for this event
        if (teamMemberRepository.existsByUserAndTeam_Event(currentUser, event)) {
            throw new ApiException(
                    "You are already in a team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Guard: team is full
        long currentCount = teamMemberRepository.countByTeam(team);
        if (currentCount >= event.getMaxTeamSize()) {
            throw new ApiException(
                    "Team is full — maximum " + event.getMaxTeamSize() + " members allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        teamMemberRepository.save(
                TeamMember.builder()
                        .team(team)
                        .user(currentUser)
                        .build()
        );

        return mapToResponse(team);
    }

    // =========================================================================
    // JOIN TEAM BY ID (kept for backward compatibility)
    // =========================================================================

    /**
     * Adds the current student to a team by team ID.
     * Used by the existing "Join" button in the teams list.
     */
    @Transactional
    public String joinTeam(Long teamId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can join teams", HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Cannot join team — scoring is locked",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (teamMemberRepository.existsByUserAndTeam_Event(currentUser, event)) {
            throw new ApiException(
                    "You are already in a team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        long currentCount = teamMemberRepository.countByTeam(team);
        if (currentCount >= event.getMaxTeamSize()) {
            throw new ApiException(
                    "Team is full — maximum " + event.getMaxTeamSize() + " members allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        teamMemberRepository.save(
                TeamMember.builder()
                        .team(team)
                        .user(currentUser)
                        .build()
        );

        return "Joined team successfully";
    }

    // =========================================================================
    // LEAVE TEAM
    // =========================================================================

    @Transactional
    public String leaveTeam(Long teamId) {

        User currentUser = getCurrentUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        TeamMember membership = teamMemberRepository
                .findByTeamAndUser(team, currentUser)
                .orElseThrow(() ->
                        new ApiException(
                                "You are not a member of this team",
                                HttpStatus.BAD_REQUEST
                        ));

        if (team.getLeader() != null &&
                team.getLeader().getId().equals(currentUser.getId())) {
            throw new ApiException(
                    "Team leader cannot leave the team",
                    HttpStatus.BAD_REQUEST
            );
        }

        teamMemberRepository.delete(membership);

        return "Left team successfully";
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByEvent(Long eventId) {
        return teamRepository.findByEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getMyTeam(Long eventId) {

        User currentUser = getCurrentUser();

        TeamMember membership = teamMemberRepository
                .findByUserAndTeam_Event_Id(currentUser, eventId)
                .orElseThrow(() ->
                        new ApiException(
                                "You are not part of any team for this event",
                                HttpStatus.NOT_FOUND
                        ));

        return mapToResponse(membership.getTeam());
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Generates a unique 8-character uppercase alphanumeric join code.
     * Uses the first 8 characters of a random UUID (without hyphens).
     * Retries if the generated code already exists in the DB (extremely rare).
     *
     * Example output: "A3F9B2C1"
     */
    private String generateUniqueJoinCode() {
        String code;
        int maxAttempts = 10;

        do {
            // UUID gives us a random string like "a3f9b2c1-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
            // Take first 8 chars after removing hyphens and uppercase it
            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

            maxAttempts--;
        } while (teamRepository.existsByJoinCode(code) && maxAttempts > 0);

        return code;
    }

    /**
     * Maps a {@link Team} entity to a {@link TeamResponse} DTO.
     */
    private TeamResponse mapToResponse(Team team) {

        List<String> memberNames = teamMemberRepository.findByTeam(team)
                .stream()
                .map(tm -> tm.getUser().getName() != null
                        ? tm.getUser().getName()
                        : "Unknown")
                .toList();

        Long   leaderId   = team.getLeader() != null ? team.getLeader().getId()   : null;
        String leaderName = team.getLeader() != null ? team.getLeader().getName() : null;

        return TeamResponse.builder()
                .id(team.getId())
                .teamName(team.getTeamName())
                .eventId(team.getEvent().getId())
                .leaderId(leaderId)
                .leaderName(leaderName)
                .members(memberNames)
                .joinCode(team.getJoinCode())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}