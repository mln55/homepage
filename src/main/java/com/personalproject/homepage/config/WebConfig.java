package com.personalproject.homepage.config;

import java.util.List;

import com.personalproject.homepage.config.web.SimplePageableHandlerMethodArgumentResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        /********************************************************************************
            trailing slash를 사용하지 않는다.
            true일 경우 /path와 /path/를 같은 자원으로 취급한다.
        ********************************************************************************/
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // org.springframework.data.domain.Pageable에 대한 argument resolver를 등록한다.
        resolvers.add(simplePageaHandlerMethodArgumentResolver());
    }

    @Bean
    public HandlerMethodArgumentResolver simplePageaHandlerMethodArgumentResolver() {
        return new SimplePageableHandlerMethodArgumentResolver();
    }
}
