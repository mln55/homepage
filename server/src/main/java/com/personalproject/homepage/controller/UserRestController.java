package com.personalproject.homepage.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Duration;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.config.jwt.JwtTokenConfig;
import com.personalproject.homepage.dto.LoginRequestDto;
import com.personalproject.homepage.dto.LoginResultDto;
import com.personalproject.homepage.entity.User;
import com.personalproject.homepage.security.jwt.JwtAssistor;
import com.personalproject.homepage.security.jwt.JwtAuthenticationToken;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final AuthenticationManager authenticationManager;

    private final JwtAssistor jwtAssistor;

    private final JwtTokenConfig jwtTokenConfig;

    // 로그인 시 토큰을 쿠키에 저장하고 유저 정보를 반환한다.
    @PostMapping("/login")
    public ApiResult<LoginResultDto> login(
        @RequestBody(required = false) LoginRequestDto loginRequest,
        HttpServletResponse response
    ) {
        checkArgument(loginRequest != null && StringUtils.hasText(loginRequest.getId()) && StringUtils.hasText(loginRequest.getPw()),
            "아이디, 비밀번호를 입력해주세요.");

        // 인증을 위해 JwtAuthenticationProvider의 authentication이 호출 된다.
        Authentication authentication = authenticationManager.authenticate(
            new JwtAuthenticationToken(loginRequest.getId(), loginRequest.getPw()));
        User user = (User) authentication.getDetails();

        Claims claims = jwtAssistor.createClaims(user.getId(), user.getRoles());
        String token = jwtAssistor.createToken(claims);

        // 쿠키의 수명은 토큰의 유효기간으로 설정한다.
        ResponseCookie tokenCookie = ResponseCookie
            .from(jwtTokenConfig.getName(), token)
            .sameSite("Strict")
            .httpOnly(true)
            .path("/")
            .maxAge(Duration.ofMillis(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()))
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        return ApiResult.success(new LoginResultDto(user.getId()));
    }
}
