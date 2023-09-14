package com.rednet.sessionservice.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSessionRequestBody(
    @NotBlank(message = "UserID should be not blank") String userID,
    @Size(min = 1, message = "There is should be at least one role") String[] roles
) {}
