package com.rednet.sessionservice.payload.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshSessionRequestBody(
        @NotBlank(message = "Token should be not blank")
        String refreshToken
) {}
