package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final ScoreRepository scoreRepository;
    private final EventRepository eventRepository;

    /**
     * Public results (any logged-in user). Only available after the organizer
     * locks scoring; returns the top 10.
     */
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ApiException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.isScoringLocked()) {
            throw new ApiException("Results will be available once judging is complete",
                    HttpStatus.BAD_REQUEST);
        }

        List<LeaderboardResponse> leaderboard = buildLeaderboard(eventId);

        return leaderboard.size() > 10
                ? leaderboard.subList(0, 10)
                : leaderboard;
    }

    /**
     * Ranks teams by their average total per judge, so a team scored by three
     * judges is not ahead of one scored by two just because it got more scores.
     * Equal scores share a rank (1, 1, 3, ...).
     */
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> buildLeaderboard(Long eventId) {

        record Row(Long teamId, String teamName, double score, long judges) {}

        List<Row> rows = new ArrayList<>();
        for (Object[] r : scoreRepository.calculateLeaderboard(eventId)) {
            long sum = ((Number) r[2]).longValue();
            long judges = Math.max(1, ((Number) r[3]).longValue());
            double avg = Math.round((sum * 100.0) / judges) / 100.0;
            rows.add(new Row((Long) r[0], (String) r[1], avg, judges));
        }

        rows.sort(Comparator.comparingDouble(Row::score).reversed()
                .thenComparing(Row::teamName, String.CASE_INSENSITIVE_ORDER));

        List<LeaderboardResponse> leaderboard = new ArrayList<>();
        int rank = 0;
        Double previous = null;

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (previous == null || Double.compare(row.score(), previous) != 0) {
                rank = i + 1;
                previous = row.score();
            }
            leaderboard.add(
                    LeaderboardResponse.builder()
                            .teamId(row.teamId())
                            .teamName(row.teamName())
                            .totalScore(row.score())
                            .judgeCount(row.judges())
                            .rank(rank)
                            .build()
            );
        }

        return leaderboard;
    }
}
