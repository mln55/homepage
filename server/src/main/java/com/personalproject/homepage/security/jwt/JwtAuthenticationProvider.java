package com.personalproject.homepage.security.jwt;

import static com.google.common.base.Preconditions.checkArgument;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.personalproject.homepage.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Value("${security.admin.id}")
    private String adminId;

    @Value("${security.admin.pw}")
    private String adminPw;

    @Value("${security.admin.roles}")
    private String adminRoles;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // supports 메소드를 통과했기에 캐스팅이 가능하다.
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;

        // 로그인 로직 수행
        // 현재 유저는 admin뿐이므로 service layer를 거치지 않고 적합한 지를 판단한다.
        String id = authenticationToken.getName();
        String pw = authenticationToken.getCredentials();

        checkArgument(adminId.equals(id) && passwordEncoder.matches(pw, adminPw), "아이디, 비밀번호를 확인해주세요.");
        User user = new User(id, pw, adminRoles);

        JwtAuthenticationToken authenticated = new JwtAuthenticationToken(
            user.getId(), null, AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles()));
        authenticated.setDetails(user);
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.equals(authentication);
    }

}
