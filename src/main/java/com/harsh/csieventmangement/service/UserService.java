package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.UpdateUserRoleRequest;
import com.harsh.csieventmangement.dto.response.UserResponse;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.*;
import com.harsh.csieventmangement.security.CurrentUser;
import com.harsh.csieventmangement.security.CustomUserDetailsService;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ScoreRepository scoreRepository;
    private final JudgeAssignmentRepository judgeAssignmentRepository;
    private final EventJudgeRepository eventJudgeRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventRoleAssignmentRepository roleAssignmentRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;


    @Transactional
    public String updateUserRole(UpdateUserRoleRequest request) {

        User currentUser = CurrentUser.get();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can update roles", HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == Role.ORGANIZER) {
            throw new ApiException(
                    "Cannot modify another ORGANIZER's role",
                    HttpStatus.BAD_REQUEST
            );
        }

        Role previousRole = user.getRole();

        user.setRole(request.getRole());
        userRepository.save(user);

        // Someone who is no longer a judge should not stay assigned to events.
        // Scores they already gave are kept.
        if (previousRole == Role.JUDGE && request.getRole() != Role.JUDGE) {
            judgeAssignmentRepository.deleteByJudge(user);
            eventJudgeRepository.deleteByJudge(user);
        }

        // The user's next request must see the new role
        userDetailsService.evict(user.getEmail());

        return "User role updated to " + request.getRole();
    }


    public List<UserResponse> getAllJudges() {
        return userRepository.findByRoleOrderByNameAsc(Role.JUDGE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public List<UserResponse> getAllNonOrganizerUsers() {
        return userRepository.findByRoleNotOrderByNameAsc(Role.ORGANIZER)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /** Profile of the logged-in user (also lets the app refresh a changed role). */
    public UserResponse getMe() {
        return mapToResponse(CurrentUser.get());
    }

    /**
     * Permanently deletes the logged-in user's account and personal data.
     * Required by Google Play for apps that let users create accounts.
     *
     * - Teams they lead are handed to the next member, or deleted if empty.
     * - Their team memberships, scores and judge assignments are removed.
     * - Events they created are kept (creator is cleared).
     */
    @Transactional
    public String deleteMyAccount(String password) {

        User user = userRepository.findById(CurrentUser.get().getId())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException("Incorrect password", HttpStatus.UNAUTHORIZED);
        }

        for (Team team : teamRepository.findByLeader(user)) {
            List<TeamMember> others = teamMemberRepository.findByTeamOrderByIdAsc(team)
                    .stream()
                    .filter(tm -> !tm.getUser().getId().equals(user.getId()))
                    .toList();

            if (others.isEmpty()) {
                scoreRepository.deleteByTeam(team);
                judgeAssignmentRepository.deleteByTeam(team);
                teamMemberRepository.deleteByTeam(team);
                teamRepository.delete(team);
            } else {
                team.setLeader(others.get(0).getUser());
                teamRepository.save(team);
            }
        }

        teamMemberRepository.deleteAll(teamMemberRepository.findByUser(user));
        scoreRepository.deleteByJudge(user);
        judgeAssignmentRepository.deleteByJudge(user);
        eventJudgeRepository.deleteByJudge(user);
        registrationRepository.deleteByUser(user);
        roleAssignmentRepository.deleteByUser(user);
        eventRepository.clearCreator(user);

        userRepository.delete(user);
        userDetailsService.evict(user.getEmail());

        return "Your account has been deleted";
    }


    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
