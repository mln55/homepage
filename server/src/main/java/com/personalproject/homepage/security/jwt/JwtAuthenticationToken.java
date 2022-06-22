package com.personalproject.homepage.security.jwt;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

// authentication 구현체 중 details가 있는 클래스를 상속한다.
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private String credentials;

    public JwtAuthenticationToken(String principal, String credentials) {
        super(null);
        super.setAuthenticated(false);
        this.principal = principal;
        this.credentials = credentials;
    }

    JwtAuthenticationToken(String principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    // override하여 생성자로만 authenticated을 설정하도록 한다.
    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        checkArgument(!isAuthenticated, "Token의 authenticated을 직접 true로 할 수 없습니다.\n" +
            "생성자를 통해 인증 여부를 설정해야 합니다.");
        super.setAuthenticated(isAuthenticated);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }


}
