package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgingCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JudgingCriteriaRepository extends JpaRepository<JudgingCriteria, Long> {

    List<JudgingCriteria> findByEvent(Event event);
    List<JudgingCriteria> findByEventId(Long eventId);

}
