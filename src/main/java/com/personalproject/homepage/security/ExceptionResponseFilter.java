package com.personalproject.homepage.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.personalproject.homepage.error.ApiException;

/********************************************************************************
    SecurityFilterChain의 커스텀 filter에서 발생한 ApiException을 처리하는 filter.
    ApiResult 형식에 맞게 json response를 보낸다.
********************************************************************************/
public class ExceptionResponseFilter extends GenericFilterBean {

    private static final String JSON_ERROR_RESPONSE_FORMAT = "{\"success\":false,\"response\":null,\"error\":{\"message\":\"%s\",\"status\":%d}}";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (ApiException ae) {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setStatus(ae.getStatus().value());
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write(String.format(JSON_ERROR_RESPONSE_FORMAT, ae.getMessage(), ae.getStatus().value()));
            writer.flush();
            writer.close();
        }
    }
}
