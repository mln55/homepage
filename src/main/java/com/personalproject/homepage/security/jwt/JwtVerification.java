package com.personalproject.homepage.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JwtVerification {

    private final JwtStatus jwtStatus;

    private final Claims claims;

    public static enum JwtStatus {
        OK, EXPIERD, INVALID;
    }
}
