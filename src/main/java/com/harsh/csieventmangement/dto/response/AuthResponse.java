package com.harsh.csieventmangement.dto.response;

import com.harsh.csieventmangement.util.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private Long userId;

    private String name;

    private String email;

    private Role role;
}
