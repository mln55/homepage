package com.personalproject.homepage.config.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.personalproject.homepage.security.jwt.JwtAssistor;

@Configuration
public class JwtAssistorConfig {

    @Bean
    public JwtAssistor jwtAssistor(JwtTokenConfig jwtTokenConfig) {
        return new JwtAssistor(
            jwtTokenConfig.getSecret(),
            jwtTokenConfig.getType(),
            jwtTokenConfig.getIssuer(),
            jwtTokenConfig.getExpirySeconds()
        );
    }
}
