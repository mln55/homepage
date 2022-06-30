package com.personalproject.homepage.security.jwt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

import com.personalproject.homepage.security.jwt.JwtVerification.JwtStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtAssistor {

    private final String secret;

    private final String type;

    private final String issuer;

    private final int expirySeconds;

    private final Key signKey;

    private final JwtParser jwtParser;

    public JwtAssistor(String secret, String type, String issuer, int expirySeconds) {
        this.secret = secret;
        this.issuer = issuer;
        this.type = type;
        this.expirySeconds = expirySeconds;
        this.signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
        this.jwtParser = Jwts.parserBuilder()
            .setSigningKey(secret)
            .requireIssuer(issuer)
            .build();
    }

    public String createToken(Claims claims) {
        return Jwts.builder()
            .signWith(signKey)
            .setHeaderParam("typ", type)
            .addClaims(claims)
            .compact();
    }

    public JwtVerification verifyToken(String token) {
        try {
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            JwsHeader<?> header = jws.getHeader();
            Claims claims = jws.getBody();

            checkArgument(type.equals(header.getType()));
            checkNotNull(claims.get("id"));
            checkNotNull(claims.get("roles"));
            return new JwtVerification(JwtStatus.OK, claims);
        } catch (ExpiredJwtException eje) {
            return new JwtVerification(JwtStatus.EXPIERD, null);
        } catch (Exception e) {
            return new JwtVerification(JwtStatus.INVALID, null);
        }
    }

    public Claims createClaims(String id, String roles) {
        Date now = new Date();
        Claims claims = Jwts.claims()
            .setIssuedAt(now)
            .setIssuer(issuer)
            .setExpiration(new Date(now.getTime() + expirySeconds * 1000l));
        claims.put("id", id);
        claims.put("roles", Arrays.asList(roles.split(",")));
        return claims;
    }

}
