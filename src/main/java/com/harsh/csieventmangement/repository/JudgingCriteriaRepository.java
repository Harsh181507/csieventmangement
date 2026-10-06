package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgingCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JudgingCriteriaRepository extends JpaRepository<JudgingCriteria, Long> {

    List<JudgingCriteria> findByEvent(Event event);

    List<JudgingCriteria> findByEventIdOrderByIdAsc(Long eventId);

    @Modifying
    @Query("DELETE FROM JudgingCriteria c WHERE c.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
