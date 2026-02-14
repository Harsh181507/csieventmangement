package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login
    Optional<User> findByEmail(String email);

    // Used to prevent duplicate registration
    boolean existsByEmail(String email);

    // Useful for admin features later
    List<User> findByRole(Role role);
}
