package com.rednet.sessionservice.exception;

import java.util.List;

public abstract class NotFoundException extends HandableException{
    public NotFoundException(List<String> messages) {
        super(messages);
    }
}
