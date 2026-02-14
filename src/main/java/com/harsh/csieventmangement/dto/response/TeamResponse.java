package com.harsh.csieventmangement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TeamResponse {

    private Long id;

    private String teamName;

    private Long eventId;

    private String eventTitle;

    private Long leaderId;

    private String leaderName;

    private List<MemberInfo> members;

    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long userId;
        private String name;
        private String email;
    }
}
