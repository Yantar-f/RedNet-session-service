package com.rednet.sessionservice.util.impl;

import com.rednet.sessionservice.util.SessionKeyGenerator;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SessionKeyGeneratorImpl implements SessionKeyGenerator {
    private final int rangeMin = 10000000;
    private final int rangeMax = 100000000;

    private final Random random = new Random();
    @Override
    public String generate() {
        return String.valueOf(random.nextInt(rangeMax - rangeMin) + rangeMin);
    }

    @Override
    public int getKeyLength() {
        return 8;
    }
}
