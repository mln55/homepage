package com.personalproject.homepage.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.personalproject.homepage.config.web.CategoryDtoHandlerMethodArgumentResolver;
import com.personalproject.homepage.config.web.SimplePageableHandlerMethodArgumentResolver;
import com.personalproject.homepage.config.web.ViewPageModelHandlerInterceptor;

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
    public void addInterceptors(InterceptorRegistry registry) {
        // 특정 경로를 제외한 요청에 대한 handlerInterceptor를 등록한다.
        registry.addInterceptor(viewPageModelHandlerInterceptor())
            .excludePathPatterns(
                "/favicon.ico", "/robots.txt", "/api/**", "/static/**"
        );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 정적 리소스에 대한 매핑. @Controller보다 먼저 작동하도록 설정한다.
        registry.setOrder(Integer.MIN_VALUE);
        registry.addViewController("/favicon.ico").setViewName("forward:/static/favicon.ico");
        registry.addViewController("/robots.txt").setViewName("forward:/static/robots.txt");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // org.springframework.data.domain.Pageable에 대한 argument resolver를 등록한다.
        resolvers.add(simplePageaHandlerMethodArgumentResolver());
        resolvers.add(categoryDtoHandlerMethodArgumentResolver());
    }

    @Bean
    public HandlerMethodArgumentResolver simplePageaHandlerMethodArgumentResolver() {
        return new SimplePageableHandlerMethodArgumentResolver();
    }

    @Bean
    public HandlerMethodArgumentResolver categoryDtoHandlerMethodArgumentResolver() {
        return new CategoryDtoHandlerMethodArgumentResolver();
    }

    @Bean
    public HandlerInterceptor viewPageModelHandlerInterceptor() {
        return new ViewPageModelHandlerInterceptor();
    }
}
