package com.rednet.sessionservice.exception;

import java.time.Instant;

public record ErrorResponseMessage(String status, Instant timestamp, String path, String message) {
}
