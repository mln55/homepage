package com.personalproject.homepage.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final String jwtName;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 클라이언트에 저장된 jwt 쿠키 삭제
        ResponseCookie tokenCookie = ResponseCookie
            .from(jwtName, null)
            .path("/")
            .maxAge(0)
            .build();
        response.setContentType("application/json");
        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        PrintWriter writer = response.getWriter();
        writer.write("{\"success\":true,\"response\":\"로그아웃 성공\",\"error\":null}");
        writer.close();
        writer.flush();
        log.info("Logout success: {}", authentication);
    }

}
