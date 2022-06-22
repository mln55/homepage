package com.personalproject.homepage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.personalproject.homepage.config.jwt.JwtTokenConfig;
import com.personalproject.homepage.security.AccessDeniedHandlerImpl;
import com.personalproject.homepage.security.AuthenticationEntryPointImpl;
import com.personalproject.homepage.security.ExceptionResponseFilter;
import com.personalproject.homepage.security.LogoutSuccessHandlerImpl;
import com.personalproject.homepage.security.jwt.JwtAssistor;
import com.personalproject.homepage.security.jwt.JwtAuthenticationFilter;
import com.personalproject.homepage.security.jwt.JwtAuthenticationProvider;
import com.personalproject.homepage.security.jwt.JwtLogoutFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAssistor jwtAssistor;

    private final JwtTokenConfig jwtTokenConfig;

    // TODO - CORS 동작 이해 및 필요성 확인후 주석 제거 or 삭제
    // @Bean
    // public CorsConfigurationSource configurationSource() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowCredentials(true);
    //     config.addAllowedOrigin("*");
    //     config.addAllowedHeader("*");
    //     config.addAllowedMethod("*");
    //     source.registerCorsConfiguration("/api/**", config);
    //     return source;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        // authenticationManager.athenticate()을 호출하기 위해 Bean으로 등록
        return super.authenticationManagerBean();
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(passwordEncoder());
    }

    @Bean
    public AuthenticationEntryPointImpl authenticationEntryPointImpl() {
        return new AuthenticationEntryPointImpl();
    }

    @Bean
    public AccessDeniedHandlerImpl accessDeniedHandlerImpl() {
        return new AccessDeniedHandlerImpl();
    }

    @Bean
    public LogoutSuccessHandlerImpl logoutSuccessHandlerImpl() {
        return new LogoutSuccessHandlerImpl(jwtTokenConfig.getName());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(new ExceptionResponseFilter(), SecurityContextPersistenceFilter.class)
            .addFilterAfter(new JwtAuthenticationFilter(jwtAssistor, jwtTokenConfig.getName()),
                SecurityContextPersistenceFilter.class)
            .addFilterBefore(new JwtLogoutFilter(), LogoutFilter.class)
            // .cors().configurationSource(configurationSource())
            //     .and()
            .csrf().disable()
            .headers().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .httpBasic().disable()
            .formLogin().disable()
            .logout()
                .logoutUrl("/api/users/logout")
                .logoutSuccessHandler(logoutSuccessHandlerImpl())
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPointImpl())
                .accessDeniedHandler(accessDeniedHandlerImpl())
                .and()
            .authorizeRequests()
                .antMatchers("/api/categories/**").hasRole("ADMIN")
                .antMatchers("/api/posts/**").hasRole("ADMIN")
                .antMatchers("/api/users/login").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            ;
    }
}
