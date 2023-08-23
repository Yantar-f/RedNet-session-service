package com.rednet.sessionservice.exception;

import java.util.List;

public abstract class BadRequestException extends HandableException {
    public BadRequestException(List<String> messages) {
        super(messages);
    }
}
