package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.TeamMemberRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // ✅ Add Member
    public String addMember(Long teamId, Long userId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.STUDENT) {
            throw new ApiException("Only STUDENTS can join teams", HttpStatus.FORBIDDEN);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ApiException("Team not found", HttpStatus.NOT_FOUND));

        Event event = team.getEvent();

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

        // ❌ Already in this team
        if (teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new ApiException("User already in this team", HttpStatus.BAD_REQUEST);
        }

        // ❌ Already in another team
        if (teamMemberRepository.existsByUser(user)) {
            throw new ApiException(
                    "User already belongs to another team",
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
    public String removeMember(Long teamMemberId) {

        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() ->
                        new ApiException("Team member not found", HttpStatus.NOT_FOUND));

        // ❌ Prevent removing leader
        if (member.getTeam().getLeader().getId().equals(member.getUser().getId())) {
            throw new ApiException("Cannot remove team leader", HttpStatus.BAD_REQUEST);
        }

        teamMemberRepository.delete(member);

        return "Member removed successfully";
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
