package com.personalproject.homepage.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        /********************************************************************************
            FilterSecurityInterceptor에서 Exception catch 및 throw,
            ExceptionTranslationFilter.sendStartAuthentication 에서
            authentication을 null로 하기에 IP를 로그로 남긴다.
                - SEC-112: Clear the SecurityContextHolder's Authentication, as the
                existing Authentication is no longer considered valid
        ********************************************************************************/
        String remoteIp = request.getHeader("X-Forwarded-For");
        if (remoteIp == null) remoteIp = request.getRemoteAddr();
        log.info("Unauthorized, URI: {}, IP: {}", request.getRequestURI(), remoteIp);

        // TODO: method is 'GET' -> response error page
        if (request.getMethod().equals("GET")) {
            response.setContentType("text/html; charset=utf8");
            // todo - 에러 페이지 응답
            response.setStatus(ErrorMessage.UNAUTHORIZED.getStatus().value());
            PrintWriter writer = response.getWriter();
                writer.write("인증이 필요한 서비스");
                writer.flush();
                writer.close();
        // Method is not 'GET' -> throw 401
        } else {
            throw new ApiException(ErrorMessage.UNAUTHORIZED);
        }

    }
}
