package com.personalproject.homepage.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.info("Access denied! URI: '{}', authentication: '{}'", request.getRequestURI(), SecurityContextHolder.getContext().getAuthentication());
        // TODO: method is 'GET' -> response erorr page
        if (request.getMethod().equals("GET")) {
            PrintWriter writer = response.getWriter();
            response.setStatus(ErrorMessage.ACCESS_DENIED.getStatus().value());
            writer.write("접근 권한 없음");
            writer.flush();
            writer.close();
        // Method is not 'GET' -> throw 403
        } else {
            throw new ApiException(ErrorMessage.ACCESS_DENIED);
        }

    }
}
