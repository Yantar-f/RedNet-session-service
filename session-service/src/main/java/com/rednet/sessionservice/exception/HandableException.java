package com.rednet.sessionservice.exception;

import jakarta.annotation.Nonnull;

import java.util.List;

public abstract class HandableException extends RuntimeException{
    List<String> messages;

    public HandableException(@Nonnull List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
