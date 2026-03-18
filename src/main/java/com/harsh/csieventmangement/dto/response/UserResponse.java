package com.harsh.csieventmangement.dto.response;

import com.harsh.csieventmangement.util.Role;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long   id;
    private String name;
    private String email;

    private Role role;
}