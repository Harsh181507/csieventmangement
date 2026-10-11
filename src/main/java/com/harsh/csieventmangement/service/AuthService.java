package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.request.LoginRequest;
import com.harsh.csieventmangement.dto.request.RegisterRequest;
import com.harsh.csieventmangement.dto.response.AuthResponse;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.UserRepository;
import com.harsh.csieventmangement.security.JwtUtil;
import com.harsh.csieventmangement.security.LoginAttemptService;
import com.harsh.csieventmangement.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttempts;

    /**
     * Email domains allowed to create accounts, e.g. "vitapstudent.ac.in".
     * Empty means any domain. Existing accounts can always log in.
     */
    @Value("${app.allowed-email-domains:}")
    private List<String> allowedEmailDomains;

    // 🔹 Register (ALWAYS STUDENT)
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());

        requireAllowedDomain(email);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)   // 🔥 FIXED
                .build();

        userRepository.save(user);

        return toAuthResponse(user);
    }

    // 🔹 Login
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        loginAttempts.checkAllowed(email);

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttempts.recordFailure(email);
            throw new ApiException(
                    "Invalid email or password",
                    HttpStatus.UNAUTHORIZED
            );
        }

        loginAttempts.reset(email);

        return toAuthResponse(user);
    }

    /** Token + profile for a user, e.g. after they change their password. */
    public AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void requireAllowedDomain(String email) {
        List<String> domains = allowedEmailDomains == null ? List.of() : allowedEmailDomains.stream()
                .map(d -> d.trim().toLowerCase(Locale.ROOT).replaceFirst("^@", ""))
                .filter(d -> !d.isEmpty())
                .toList();

        if (domains.isEmpty() || domains.stream().anyMatch(d -> email.endsWith("@" + d))) {
            return;
        }

        throw new ApiException(
                "Please sign up with your college email (ending in @" + String.join(" or @", domains) + ")",
                HttpStatus.BAD_REQUEST
        );
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
