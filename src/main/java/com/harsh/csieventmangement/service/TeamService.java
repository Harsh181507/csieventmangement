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
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Business logic for all team operations.
 *
 * <p><strong>Join Code feature:</strong>
 * When a team is created, an 8-character unique uppercase code is generated
 * using the first 8 characters of a UUID (e.g. "A3F9B2C1"). This code is
 * stored on the team and returned in the API response. The leader shares
 * this code with teammates who call POST /teams/join-by-code to join.
 * The code is only returned to members of that team (and organizers), so
 * other students cannot join a team without being invited.
 *
 * <p><strong>Concurrency:</strong>
 * Create/join lock the student's user row, and joins lock the team row, so
 * double taps or many students joining at once cannot exceed the team size
 * or put a student in two teams.
 *
 * <p><strong>File:</strong>
 * {@code src/main/java/com/harsh/csieventmangement/service/TeamService.java}
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int MAX_TEAM_NAME_LENGTH = 100;

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

        User currentUser = lockCurrentStudent("create teams");

        String name = teamName == null ? "" : teamName.trim();
        if (name.isEmpty()) {
            throw new ApiException("Team name is required", HttpStatus.BAD_REQUEST);
        }
        if (name.length() > MAX_TEAM_NAME_LENGTH) {
            throw new ApiException(
                    "Team name cannot exceed " + MAX_TEAM_NAME_LENGTH + " characters",
                    HttpStatus.BAD_REQUEST
            );
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

        if (teamRepository.existsByTeamNameIgnoreCaseAndEvent(name, event)) {
            throw new ApiException(
                    "A team with this name already exists for this event",
                    HttpStatus.CONFLICT
            );
        }

        // Generate a unique 8-character uppercase join code
        String joinCode = generateUniqueJoinCode();

        Team team = Team.builder()
                .teamName(name)
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

        return mapToResponses(List.of(savedTeam), currentUser).get(0);
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

        User currentUser = lockCurrentStudent("join teams");

        String normalized = code == null ? "" : code.trim().toUpperCase();

        // Uppercase the input so codes are case-insensitive
        Team found = teamRepository.findByJoinCode(normalized)
                .orElseThrow(() ->
                        new ApiException(
                                "Invalid join code — double check and try again",
                                HttpStatus.NOT_FOUND
                        ));

        Team team = addMember(found.getId(), currentUser);

        return mapToResponses(List.of(team), currentUser).get(0);
    }

    // =========================================================================
    // JOIN TEAM BY ID (kept for backward compatibility)
    // =========================================================================

    /**
     * Adds the current student to a team by team ID.
     */
    @Transactional
    public String joinTeam(Long teamId) {

        User currentUser = lockCurrentStudent("join teams");

        addMember(teamId, currentUser);

        return "Joined team successfully";
    }

    // =========================================================================
    // LEAVE TEAM
    // =========================================================================

    @Transactional
    public String leaveTeam(Long teamId) {

        User currentUser = CurrentUser.get();

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
        return mapToResponses(
                teamRepository.findByEventIdWithLeader(eventId),
                CurrentUser.get()
        );
    }

    @Transactional(readOnly = true)
    public TeamResponse getMyTeam(Long eventId) {

        User currentUser = CurrentUser.get();

        TeamMember membership = teamMemberRepository
                .findByUserAndTeam_Event_Id(currentUser, eventId)
                .orElseThrow(() ->
                        new ApiException(
                                "You are not part of any team for this event",
                                HttpStatus.NOT_FOUND
                        ));

        return mapToResponses(List.of(membership.getTeam()), currentUser).get(0);
    }

    /** Every team the current user belongs to, newest first. */
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeams() {

        User currentUser = CurrentUser.get();

        List<Team> teams = teamMemberRepository.findByUserWithTeam(currentUser)
                .stream()
                .map(TeamMember::getTeam)
                .toList();

        return mapToResponses(teams, currentUser);
    }

    /**
     * Maps teams to responses with 2 queries in total (members + their users),
     * instead of several queries per team.
     *
     * @param viewer the user the response is for; join codes are only included
     *               for teams the viewer belongs to, or when the viewer is an organizer
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> mapToResponses(List<Team> teams, User viewer) {

        if (teams.isEmpty()) {
            return List.of();
        }

        Map<Long, List<TeamMember>> membersByTeam = new LinkedHashMap<>();
        for (TeamMember tm : teamMemberRepository.findByTeamInWithUser(teams)) {
            membersByTeam
                    .computeIfAbsent(tm.getTeam().getId(), id -> new ArrayList<>())
                    .add(tm);
        }

        boolean organizer = viewer != null && viewer.getRole() == Role.ORGANIZER;

        return teams.stream().map(team -> {
            List<TeamMember> members = membersByTeam.getOrDefault(team.getId(), List.of());

            boolean viewerIsMember = viewer != null && members.stream()
                    .anyMatch(tm -> tm.getUser().getId().equals(viewer.getId()));

            List<String> memberNames = members.stream()
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
                    .joinCode(organizer || viewerIsMember ? team.getJoinCode() : null)
                    .build();
        }).toList();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Adds a student to a team while holding a lock on the team row, so the
     * size check and insert cannot interleave with another join.
     */
    private Team addMember(Long teamId, User student) {

        Team team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException(
                    "Cannot join team — scoring is locked for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Guard: student already in a team for this event
        if (teamMemberRepository.existsByUserAndTeam_Event(student, event)) {
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
                        .user(student)
                        .build()
        );

        return team;
    }

    /**
     * Returns the current user after locking their row for this transaction.
     * Ensures only students reach team create/join logic.
     */
    private User lockCurrentStudent(String action) {

        User currentUser = CurrentUser.get();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENT can " + action, HttpStatus.FORBIDDEN);
        }

        return userRepository.findByIdForUpdate(currentUser.getId())
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

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
}
