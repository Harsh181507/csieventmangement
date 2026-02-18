package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgeAssignment;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, Long> {

    Optional<JudgeAssignment> findByTeamAndJudge(Team team, User judge);

    List<JudgeAssignment> findByJudgeAndEvent(User judge, Event event);
}