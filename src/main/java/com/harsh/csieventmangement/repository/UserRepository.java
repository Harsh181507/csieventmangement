package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.User;
import com.harsh.csieventmangement.util.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login (emails are matched case-insensitively)
    Optional<User> findByEmailIgnoreCase(String email);

    // Used to prevent duplicate registration
    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleOrderByNameAsc(Role role);

    List<User> findByRoleNotOrderByNameAsc(Role role);

    /**
     * Locks the user's row until the transaction ends. Serialises one user's
     * team create/join requests so a double tap cannot put them in two teams.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
