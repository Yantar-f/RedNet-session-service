package com.rednet.sessionservice.util;

public interface SessionKeyGenerator {
    String generate();
    int getKeyLength();
}
