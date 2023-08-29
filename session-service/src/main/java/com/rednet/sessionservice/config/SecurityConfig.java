package com.rednet.sessionservice.config;

import com.rednet.sessionservice.filter.ApiTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ApiTokenFilter apiTokenFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
        ApiTokenFilter apiTokenFilter,
        AccessDeniedHandler accessDeniedHandler,
        AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.apiTokenFilter = apiTokenFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .csrf(CsrfConfigurer::disable)
            .cors(CorsConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll())
                    //.anyRequest().authenticated())
            .exceptionHandling(exHandle -> exHandle
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint))
            .addFilterBefore(apiTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
