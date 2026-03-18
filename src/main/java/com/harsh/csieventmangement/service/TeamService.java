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

/**
 * Business logic for all team operations.
 *
 * <p><strong>Why TeamMemberRepository instead of team.getMembers():</strong>
 * The {@code Team} entity previously had a {@code @ManyToMany Set<User> members}
 * field. This was removed because the {@code team_members} DB table has its own
 * {@code id} column, which is incompatible with JPA's pure join table requirement
 * for {@code @ManyToMany}.
 *
 * <p>All membership reads and writes now go through {@link TeamMemberRepository}
 * using the {@link TeamMember} entity as the single source of truth.
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
    private final TeamMemberRepository teamMemberRepository; // ← replaces team.getMembers()

    // =========================================================================
    // CREATE TEAM
    // =========================================================================

    /**
     * Creates a new team for the given event. The calling student is
     * automatically set as leader and added as the first member.
     */
    @Transactional
    public String createTeam(Long eventId, String teamName) {

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

        // FIX: was teamRepository.findTeamByUserAndEvent() — that method was
        // removed from TeamRepository because it navigated through the deleted
        // @ManyToMany members field. Now we check via TeamMemberRepository.
        if (teamMemberRepository.existsByUserAndTeam_Event(currentUser, event)) {
            throw new ApiException(
                    "You are already in a team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Create and save the team
        Team team = Team.builder()
                .teamName(teamName)
                .event(event)
                .leader(currentUser)
                .build();

        Team savedTeam = teamRepository.save(team);

        // FIX: was team.getMembers().add(user) — members Set was removed from Team.
        // Now add the creator as first member via TeamMember entity.
        TeamMember leaderMembership = TeamMember.builder()
                .team(savedTeam)
                .user(currentUser)
                .build();

        teamMemberRepository.save(leaderMembership);

        return "Team created successfully";
    }

    // =========================================================================
    // JOIN TEAM
    // =========================================================================

    /**
     * Adds the current student to an existing team.
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

        // FIX: same as above — use TeamMemberRepository instead of
        // teamRepository.findTeamByUserAndEvent()
        if (teamMemberRepository.existsByUserAndTeam_Event(currentUser, event)) {
            throw new ApiException(
                    "You are already in a team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        // FIX: was team.getMembers().size() — members Set removed from Team.
        // Use DB count via TeamMemberRepository instead.
        long currentMemberCount = teamMemberRepository.countByTeam(team);
        if (currentMemberCount >= event.getMaxTeamSize()) {
            throw new ApiException(
                    "Team is full — maximum " + event.getMaxTeamSize() + " members allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Add new member via TeamMember entity
        TeamMember membership = TeamMember.builder()
                .team(team)
                .user(currentUser)
                .build();

        teamMemberRepository.save(membership);

        return "Joined team successfully";
    }

    // =========================================================================
    // LEAVE TEAM
    // =========================================================================

    /**
     * Removes the current student from a team.
     * The team leader cannot leave.
     */
    @Transactional
    public String leaveTeam(Long teamId) {

        User currentUser = getCurrentUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        // FIX: was team.getMembers().contains(currentUser) — members Set removed.
        // Look up the membership record directly instead.
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

        // FIX: was team.getMembers().remove() — now delete the TeamMember record
        teamMemberRepository.delete(membership);

        return "Left team successfully";
    }

    // =========================================================================
    // GET TEAMS BY EVENT
    // =========================================================================

    /**
     * Returns all teams for a given event with their member names.
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByEvent(Long eventId) {
        return teamRepository.findByEventId(eventId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================================
    // GET MY TEAM
    // =========================================================================

    /**
     * Returns the team the current student belongs to for a given event.
     */
    @Transactional(readOnly = true)
    public TeamResponse getMyTeam(Long eventId) {

        User currentUser = getCurrentUser();

        // FIX: was teamRepository.findByMembers_IdAndEvent_Id() — that method
        // navigated through the deleted @ManyToMany members field.
        // Now find the TeamMember record and get the team from it.
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
     * Maps a {@link Team} entity to a {@link TeamResponse} DTO.
     * Fetches member names via {@link TeamMemberRepository}.
     */
    private TeamResponse mapToResponse(Team team) {

        // FIX: was team.getMembers().stream()... — members Set removed from Team.
        // Fetch member names via TeamMemberRepository instead.
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
                .build();
    }

    /**
     * Gets the currently authenticated user from the Spring Security context.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}