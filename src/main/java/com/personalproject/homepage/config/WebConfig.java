package com.personalproject.homepage.config;

import org.springframework.context.annotation.Configuration;
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

}
