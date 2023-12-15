package com.rednet.sessionservice.util;

public interface TokenIDGenerator {
    String generate();
    int getIDLength();
}
