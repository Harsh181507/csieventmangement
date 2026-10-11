package com.harsh.csieventmangement.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.harsh.csieventmangement.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * Slows down password guessing: after {@value #MAX_FAILURES} wrong passwords
 * for an email, password checks for it are refused for {@link #LOCK_TIME}
 * (counted from the last wrong attempt). In memory, so a restart clears it.
 */
@Component
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private final Cache<String, Integer> failures = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(LOCK_TIME)
            .build();

    /** Throws 429 while the email is locked out. Call before checking a password. */
    public void checkAllowed(String email) {
        Integer count = failures.getIfPresent(key(email));
        if (count != null && count >= MAX_FAILURES) {
            throw new ApiException(
                    "Too many wrong passwords. Try again in " + LOCK_TIME.toMinutes() + " minutes.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    public void recordFailure(String email) {
        failures.asMap().merge(key(email), 1, Integer::sum);
    }

    public void reset(String email) {
        failures.invalidate(key(email));
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
