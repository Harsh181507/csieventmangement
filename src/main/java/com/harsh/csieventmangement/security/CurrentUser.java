package com.harsh.csieventmangement.security;

import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Access to the user making the current request.
 *
 * The user is already loaded by {@link JwtAuthenticationFilter}, so services
 * should use this instead of looking the user up by email again.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }

        throw new ApiException("Authentication required. Please log in.", HttpStatus.UNAUTHORIZED);
    }
}
