package com.rednet.sessionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Configuration
public class AppConfig {
    public static final String DATE_PATTERN = "yyyy-MM-dd/HH:mm:ss.SSS/Z";

    @Bean
    public DateFormat dateFormat() {
        return new SimpleDateFormat(DATE_PATTERN);
    }
}
