package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.TeamMemberRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leader-managed team membership. Only a team's leader can add or remove
 * members (members can still leave by themselves via TeamService.leaveTeam).
 */
@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // ✅ Add Member
    @Transactional
    public String addMember(Long teamId, Long userId) {

        User currentUser = CurrentUser.get();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENTS can manage teams", HttpStatus.FORBIDDEN);
        }

        // Lock the team row so concurrent adds cannot exceed the size limit
        Team team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        requireLeader(team, currentUser);

        Event event = team.getEvent();

        if (event.isScoringLocked()) {
            throw new ApiException("Scoring is locked for this event", HttpStatus.BAD_REQUEST);
        }

        long currentMembers = teamMemberRepository.countByTeam(team);

        if (currentMembers >= event.getMaxTeamSize()) {
            throw new ApiException(
                    "Team member limit reached. Max allowed: " + event.getMaxTeamSize(),
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() != Role.STUDENT) {
            throw new ApiException("Only students can be added to teams", HttpStatus.BAD_REQUEST);
        }

        // ❌ Already in this team
        if (teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new ApiException("User already in this team", HttpStatus.BAD_REQUEST);
        }

        // ❌ Already in another team for this event
        if (teamMemberRepository.existsByUserAndTeam_Event(user, event)) {
            throw new ApiException(
                    "User already belongs to another team for this event",
                    HttpStatus.BAD_REQUEST
            );
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .build();

        teamMemberRepository.save(member);

        return "Member added successfully";
    }

    // ✅ Remove Member
    @Transactional
    public String removeMember(Long teamMemberId) {

        User currentUser = CurrentUser.get();

        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() ->
                        new ApiException("Team member not found", HttpStatus.NOT_FOUND));

        Team team = member.getTeam();

        requireLeader(team, currentUser);

        // ❌ Prevent removing leader
        if (team.getLeader().getId().equals(member.getUser().getId())) {
            throw new ApiException("Cannot remove team leader", HttpStatus.BAD_REQUEST);
        }

        teamMemberRepository.delete(member);

        return "Member removed successfully";
    }

    private void requireLeader(Team team, User user) {
        if (team.getLeader() == null || !team.getLeader().getId().equals(user.getId())) {
            throw new ApiException(
                    "Only the team leader can manage members",
                    HttpStatus.FORBIDDEN
            );
        }
    }
}
