package com.rednet.sessionservice.payload.response;

import java.util.List;

public record ErrorResponseMessage(String status, String timestamp, String path, List<String> messages) {
}
