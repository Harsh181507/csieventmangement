package com.harsh.csieventmangement.service;

import com.harsh.csieventmangement.dto.response.LeaderboardResponse;
import com.harsh.csieventmangement.entity.Event;
import com.harsh.csieventmangement.entity.JudgingCriteria;
import com.harsh.csieventmangement.entity.Team;
import com.harsh.csieventmangement.entity.TeamMember;
import com.harsh.csieventmangement.exception.ApiException;
import com.harsh.csieventmangement.repository.EventRepository;
import com.harsh.csieventmangement.repository.JudgingCriteriaRepository;
import com.harsh.csieventmangement.repository.ScoreRepository;
import com.harsh.csieventmangement.repository.TeamMemberRepository;
import com.harsh.csieventmangement.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds an event's full results as CSV for organizers: every team (scored or
 * not) with rank, average total, per-criterion averages and members.
 */
@Service
@RequiredArgsConstructor
public class ResultsExportService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final JudgingCriteriaRepository criteriaRepository;
    private final ScoreRepository scoreRepository;
    private final LeaderboardService leaderboardService;

    public record CsvFile(String fileName, String content) {}

    @Transactional(readOnly = true)
    public CsvFile exportResults(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException("Event not found", HttpStatus.NOT_FOUND));

        List<JudgingCriteria> criteria = criteriaRepository.findByEventIdOrderByIdAsc(eventId);
        List<Team> teams = teamRepository.findByEventIdWithLeader(eventId);

        Map<Long, List<TeamMember>> membersByTeam = new HashMap<>();
        if (!teams.isEmpty()) {
            for (TeamMember tm : teamMemberRepository.findByTeamInWithUser(teams)) {
                membersByTeam.computeIfAbsent(tm.getTeam().getId(), id -> new ArrayList<>()).add(tm);
            }
        }

        // teamId -> criteriaId -> average
        Map<Long, Map<Long, Double>> averages = new HashMap<>();
        for (Object[] row : scoreRepository.averageByTeamAndCriteria(eventId)) {
            averages.computeIfAbsent((Long) row[0], id -> new HashMap<>())
                    .put((Long) row[1], ((Number) row[2]).doubleValue());
        }

        // Ranked teams first, then teams nobody has scored yet
        Map<Long, LeaderboardResponse> ranked = new LinkedHashMap<>();
        for (LeaderboardResponse entry : leaderboardService.buildLeaderboard(eventId)) {
            ranked.put(entry.getTeamId(), entry);
        }
        Map<Long, Team> teamsById = teams.stream()
                .collect(Collectors.toMap(Team::getId, t -> t, (a, b) -> a, LinkedHashMap::new));
        List<Team> ordered = new ArrayList<>();
        ranked.keySet().forEach(id -> { if (teamsById.containsKey(id)) ordered.add(teamsById.get(id)); });
        teams.stream().filter(t -> !ranked.containsKey(t.getId())).forEach(ordered::add);

        int totalPoints = criteria.stream().mapToInt(JudgingCriteria::getMaxScore).sum();

        StringBuilder csv = new StringBuilder("﻿"); // BOM so Excel reads UTF-8 names correctly
        List<String> header = new ArrayList<>(List.of(
                "Rank", "Team", "Average total (out of " + totalPoints + ")", "Judges"));
        criteria.forEach(c -> header.add(c.getTitle() + " (out of " + c.getMaxScore() + ")"));
        header.addAll(List.of("Leader", "Members", "Member emails"));
        appendRow(csv, header);

        for (Team team : ordered) {
            LeaderboardResponse entry = ranked.get(team.getId());
            Map<Long, Double> teamAverages = averages.getOrDefault(team.getId(), Map.of());
            List<TeamMember> members = membersByTeam.getOrDefault(team.getId(), List.of());

            List<String> row = new ArrayList<>();
            row.add(entry != null ? String.valueOf(entry.getRank()) : "");
            row.add(team.getTeamName());
            row.add(entry != null ? number(entry.getTotalScore()) : "");
            row.add(entry != null ? String.valueOf(entry.getJudgeCount()) : "0");
            criteria.forEach(c -> {
                Double avg = teamAverages.get(c.getId());
                row.add(avg != null ? number(avg) : "");
            });
            row.add(team.getLeader() != null ? team.getLeader().getName() : "");
            row.add(members.stream().map(tm -> tm.getUser().getName()).collect(Collectors.joining("; ")));
            row.add(members.stream().map(tm -> tm.getUser().getEmail()).collect(Collectors.joining("; ")));
            appendRow(csv, row);
        }

        return new CsvFile(slug(event.getTitle()) + "-results.csv", csv.toString());
    }

    private static void appendRow(StringBuilder csv, List<String> cells) {
        csv.append(cells.stream().map(ResultsExportService::cell).collect(Collectors.joining(","))).append("\r\n");
    }

    /**
     * Quotes a cell when needed, and stops text such as a team named
     * "=HYPERLINK(...)" from running as a formula in Excel or Sheets.
     */
    private static String cell(String value) {
        String v = value == null ? "" : value;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private static String number(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        return rounded == Math.rint(rounded)
                ? String.valueOf((long) rounded)
                : String.format(Locale.ROOT, "%.2f", rounded).replaceAll("0+$", "");
    }

    private static String slug(String title) {
        String s = title == null ? "" : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return s.isEmpty() ? "event" : s;
    }
}
