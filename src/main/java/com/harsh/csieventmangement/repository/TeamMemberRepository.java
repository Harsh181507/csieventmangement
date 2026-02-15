package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    long countByTeam(Team team);

    boolean existsByTeamAndUser(Team team, User user);

    Optional<TeamMember> findByUser(User user);

    boolean existsByUser(User user);
}
