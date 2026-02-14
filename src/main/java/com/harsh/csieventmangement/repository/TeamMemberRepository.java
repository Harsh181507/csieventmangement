package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // Check if user already in a team
    Optional<TeamMember> findByUser(User user);

    // Get all members of a team
    List<TeamMember> findByTeam(Team team);
}
