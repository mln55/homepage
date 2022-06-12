package com.personalproject.homepage.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties( "security.jwt.token")
@Getter
@Setter
public class JwtTokenConfig {

    private String name;

    private String type;

    private String secret;

    private String issuer;

    private int expirySeconds;
}
