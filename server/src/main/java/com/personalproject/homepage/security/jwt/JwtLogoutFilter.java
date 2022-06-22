package com.personalproject.homepage.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

public class JwtLogoutFilter extends GenericFilterBean {

    private static final String LOGOUT_PROCCESSING_URI = "/api/users/logout";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        /********************************************************************************
            logoutProcessingUrl 요청은 SecurityConfig의 antMatchers에 영향을 받지 않는다.
            LogoutFilter에서 SecurityFilterChain이 끊기기에 (chain.doFilter 호출 X)
            ExceptionTranslationFilter에서 SecurityException을 catch할 수 없다.
        ********************************************************************************/
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getRequestURI().equals(LOGOUT_PROCCESSING_URI)) {

            // Authentication == null -> throw 401
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                throw new ApiException(ErrorMessage.UNAUTHORIZED);
            } else {
                // Method is not 'POST' -> throw 405
                if (!request.getMethod().equals("POST")) {
                    throw new ApiException(ErrorMessage.METHOD_NOT_ALLOWED, request.getMethod(), "POST");
                }
            }
        }

        chain.doFilter(request, response);
    }

}
