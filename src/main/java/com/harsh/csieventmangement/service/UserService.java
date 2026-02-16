package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public String updateUserRole(Long userId, Role newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (newRole == Role.ORGANIZER) {
            throw new ApiException("Cannot assign ORGANIZER role", HttpStatus.BAD_REQUEST);
        }

        user.setRole(newRole);
        userRepository.save(user);

        return "User role updated to " + newRole.name();
    }
}
