package com.harsh.csieventmangement.dto.request;

import com.harsh.csieventmangement.util.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Role role;
}
