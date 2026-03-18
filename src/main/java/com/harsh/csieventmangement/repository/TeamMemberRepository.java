package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {


    boolean existsByTeamAndUser(Team team, User user);


    boolean existsByUserAndTeam_Event(User user, Event event);


    boolean existsByUser(User user);



    Optional<TeamMember> findByTeamAndUser(Team team, User user);


    Optional<TeamMember> findByUserAndTeam_Event_Id(User user, Long eventId);


    List<TeamMember> findByTeam(Team team);

    long countByTeam(Team team);
}