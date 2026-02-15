package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;

    public String updateMaxTeamSize(Long eventId, Integer newMaxSize) {

        if (newMaxSize == null || newMaxSize <= 0) {
            throw new ApiException("Max team size must be greater than 0", HttpStatus.BAD_REQUEST);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        List<Team> teams = teamRepository.findAll()
                .stream()
                .filter(t -> t.getEvent().getId().equals(eventId))
                .toList();

        for (Team team : teams) {
            if (team.getMembers().size() > newMaxSize) {
                throw new ApiException(
                        "Cannot reduce max team size below existing team member count",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        event.setMaxTeamSize(newMaxSize);
        eventRepository.save(event);

        return "Max team size updated successfully";
    }
}
