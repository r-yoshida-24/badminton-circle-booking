package com.badminton;

import com.badminton.entity.Role;
import com.badminton.security.MemberPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestPrincipalFactory {

    private TestPrincipalFactory() {
    }

    public static MemberPrincipal principal(Long memberId, String lineUserId, String displayName, Role role) {
        return new MemberPrincipal(memberId, lineUserId, displayName, role, true);
    }

    public static RequestPostProcessor authentication(MemberPrincipal principal) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
