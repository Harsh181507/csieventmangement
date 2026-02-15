package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    // ✅ Add Member
    @PostMapping("/{teamId}/add/{userId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                teamMemberService.addMember(teamId, userId)
        );
    }

    // ✅ Remove Member
    @DeleteMapping("/{teamMemberId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> removeMember(
            @PathVariable Long teamMemberId
    ) {
        return ResponseEntity.ok(
                teamMemberService.removeMember(teamMemberId)
        );
    }
}
