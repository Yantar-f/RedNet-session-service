package com.rednet.sessionservice.model;

import jakarta.validation.constraints.NotBlank;

public record SessionRefreshingData(
        @NotBlank(message = "Token should be not blank")
        String refreshToken
) {}
