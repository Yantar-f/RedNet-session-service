package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.util.SessionPostfixGenerator;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SessionPostfixGeneratorImpl implements SessionPostfixGenerator {
    private final int rangeMin = 100000;
    private final int rangeMax = 1000000;

    private final Random random = new Random();
    @Override
    public String generate() {
        return String.valueOf(random.nextInt(rangeMax - rangeMin) + rangeMin);
    }

    @Override
    public int getPostfixLength() {
        return 6;
    }
}
