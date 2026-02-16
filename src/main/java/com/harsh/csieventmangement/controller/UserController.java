package com.harsh.csieventmangement.controller;

import com.harsh.csieventmangement.dto.request.UpdateUserRoleRequest;
import com.harsh.csieventmangement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/role")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<String> updateUserRole(
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUserRole(request)
        );
    }
}
