package com.personalproject.homepage.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.personalproject.homepage.config.jwt.JwtAssistorConfig;
import com.personalproject.homepage.config.jwt.JwtTokenConfig;

/********************************************************************************
    단위 테스트 시 SecurityConfig에서 필요한 의존성을 명시적으로 설정 한다.
********************************************************************************/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ JwtAssistorConfig.class, JwtTokenConfig.class})
public @interface CustomUnitTestSecurityConfig { }
