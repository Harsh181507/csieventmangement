package com.harsh.csieventmangement.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Short-lived cache of users by email. Every authenticated request needs the
     * user; caching it removes one DB round-trip per request. Entries are evicted
     * explicitly when a user's role changes or the account is deleted.
     */
    private final Cache<String, User> userCache = Caffeine.newBuilder()
            .maximumSize(5_000)
            .expireAfterWrite(Duration.ofSeconds(60))
            .build();

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String key = normalize(email);

        User user = userCache.getIfPresent(key);
        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(key)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));
            userCache.put(key, user);
        }

        return new CustomUserDetails(user);
    }

    /** Drops a cached user so the next request reloads it from the DB. */
    public void evict(String email) {
        if (email != null) {
            userCache.invalidate(normalize(email));
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
