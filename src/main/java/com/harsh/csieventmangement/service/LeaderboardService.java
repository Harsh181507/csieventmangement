package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final ScoreRepository scoreRepository;
    private final EventRepository eventRepository;

    public List<LeaderboardResponse> getLeaderboard(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.isScoringLocked()) {
            throw new ApiException("Leaderboard not available until scoring is locked",
                    HttpStatus.BAD_REQUEST);
        }

        List<Object[]> results = scoreRepository.calculateLeaderboard(eventId);

        List<LeaderboardResponse> leaderboard = new ArrayList<>();
        int rank = 1;

        for (Object[] row : results) {
            leaderboard.add(
                    LeaderboardResponse.builder()
                            .teamId((Long) row[0])
                            .teamName((String) row[1])
                            .totalScore(((Long) row[2]))
                            .rank(rank++)
                            .build()
            );
        }

        return leaderboard.size() > 10
                ? leaderboard.subList(0, 10)
                : leaderboard;
    }
}