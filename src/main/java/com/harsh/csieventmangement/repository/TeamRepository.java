package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {



    List<Team> findByEvent(Event event);

    List<Team> findByEventId(Long eventId);



    Optional<Team> findByLeader(User leader);


    Optional<Team> findByJoinCode(String joinCode);


    boolean existsByTeamNameAndEvent(String teamName, Event event);

    boolean existsByJoinCode(String joinCode);
}