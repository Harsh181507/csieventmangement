package com.harsh.csieventmangement.repository;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {


    boolean existsByTeamAndUser(Team team, User user);


    boolean existsByUserAndTeam_Event(User user, Event event);


    Optional<TeamMember> findByTeamAndUser(Team team, User user);


    Optional<TeamMember> findByUserAndTeam_Event_Id(User user, Long eventId);


    List<TeamMember> findByTeamOrderByIdAsc(Team team);

    List<TeamMember> findByUser(User user);

    // A user's memberships with team and leader loaded in the same query
    @Query("""
        SELECT tm FROM TeamMember tm
        JOIN FETCH tm.team t
        LEFT JOIN FETCH t.leader
        WHERE tm.user = :user
        ORDER BY t.id DESC
    """)
    List<TeamMember> findByUserWithTeam(@Param("user") User user);

    // Members of many teams with their user loaded in one query
    @Query("""
        SELECT tm FROM TeamMember tm
        JOIN FETCH tm.user
        WHERE tm.team IN :teams
        ORDER BY tm.id
    """)
    List<TeamMember> findByTeamInWithUser(@Param("teams") Collection<Team> teams);

    long countByTeam(Team team);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.id IN (SELECT t.id FROM Team t WHERE t.event.id = :eventId)")
    void deleteByEventId(@Param("eventId") Long eventId);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team = :team")
    void deleteByTeam(@Param("team") Team team);
}
