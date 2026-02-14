package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.EventJudge;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventJudgeRepository extends JpaRepository<EventJudge, Long> {

    Optional<EventJudge> findByEventAndJudge(Event event, User judge);
}