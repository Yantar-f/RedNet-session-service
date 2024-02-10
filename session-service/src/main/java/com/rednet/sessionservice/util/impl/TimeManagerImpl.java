package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.util.TimeManager;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TimeManagerImpl implements TimeManager {
    @Override
    public Instant stampTime() {
        return Instant.now();
    }
}
