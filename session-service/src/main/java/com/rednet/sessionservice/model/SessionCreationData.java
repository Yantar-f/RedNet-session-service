package com.rednet.sessionservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SessionCreationData(
    @NotBlank(message = "UserID should be not blank")
    String userID,

    @Size(min = 1, message = "There is should be at least one role")
    String[] roles
) {}
