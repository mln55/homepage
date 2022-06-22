package com.personalproject.homepage.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.net.HttpHeaders;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;
import com.personalproject.homepage.security.jwt.JwtVerification.JwtStatus;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

/********************************************************************************
    session을 사용하지 않고 각 request의 cookie를 확인하여 인증된 사용자인지를 검증한다.
    다른 필터들이 authentication을 필요로 하기 전에 작동 되어야 한다.
    request마다 새로운 SecurityContext가 생성되기에 SecurityContextPersistenceFilter뒤에 위치시킨다.
    {@link SecurityContextPersistenceFilter}
    {@link HttpSessionSecurityContextRepository}

    사용자 관리는 authentication의 principal(아이디) 및 details에 저장된
    WebAuthenticationDetails의 RemoteIpAddress를 통해 진행하면 될 것 같다.
    (익명 사용자의 경우 RemoteIpAddress만을 이용한다.)
********************************************************************************/
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtAssistor jwtAssistor;

    private final String jwtName;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        /********************************************************************************
            1. request cookie에서 token 정보를 얻는다.
            2. token이 없는 경우 로직을 수행하지 않으며 결국 authentication은 anonymous가 된다.
            3. token이 있는 경우 토근을 검증하여 authentication을 SecurityContext에 저장한다.
        ********************************************************************************/
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authorizationToken = obtainAuthorizationToken(request);
        if (authorizationToken != null) {
            JwtVerification jwtVerification = jwtAssistor.verifyToken(authorizationToken);
            JwtStatus jwtStatus = jwtVerification.getJwtStatus();
            // jwtStatus != OK이면 토큰 쿠키를 삭제한다.
            if (jwtStatus != JwtStatus.OK) {
                ResponseCookie jwtCookie = ResponseCookie
                    .from(jwtName, null)
                    .maxAge(0)
                    .path("/")
                    .build();
                response.setHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
                if (jwtStatus == JwtStatus.EXPIERD) {
                    throw new ApiException(ErrorMessage.JWT_EXPIERD);
                } else if (jwtStatus == JwtStatus.INVALID) {
                    throw new ApiException(ErrorMessage.JWT_INVALID);
                }
            }
            Claims claims = jwtVerification.getClaims();
            String principal = claims.get("id", String.class);

            @SuppressWarnings("unchecked")
            List<String> roleList = claims.get("roles", new ArrayList<String>().getClass());
            List<GrantedAuthority> authorities = roleList.isEmpty() ? AuthorityUtils.NO_AUTHORITIES
                : roleList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            // custom authentication 등록
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }


    private String obtainAuthorizationToken(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies == null ? new Cookie[]{} : cookies) {
            if (cookie.getName().equals(jwtName)) {
                token = cookie.getValue();
            }
        }
        return token == null ? null : token;
    }

}
