package com.rednet.sessionservice.config;

import com.rednet.sessionservice.service.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoutingConfig {
    private final SessionService sessionService;

    public RoutingConfig(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route(POST("/create-session"), sessionService::createSession)
            .andRoute(GET("/get-session"), sessionService::getSession)
            .andRoute(GET("/get-sessions-by-user-id"), sessionService::getSessionsByUserID)
            .andRoute(POST("/refresh-session"), sessionService::refreshSession)
            .andRoute(POST("/delete-session"), sessionService::deleteSession)
            .andRoute(POST("/delete-sessions-by-user-id"), sessionService::deleteSessionsByUserID);
    }
}
