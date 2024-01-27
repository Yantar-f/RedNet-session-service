package com.rednet.sessionservice.model;

import jakarta.validation.constraints.NotEmpty;

public record SessionRefreshingData(
        @NotEmpty(message = "Token should be not empty")
        String refreshToken
) {}
