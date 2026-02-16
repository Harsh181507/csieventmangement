package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.UpdateUserRoleRequest;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String updateUserRole(UpdateUserRoleRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new ApiException("Only ORGANIZER can update roles", HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (user.getRole() == Role.ORGANIZER) {
            throw new ApiException("Cannot modify ORGANIZER role", HttpStatus.BAD_REQUEST);
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        return "User role updated successfully";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
