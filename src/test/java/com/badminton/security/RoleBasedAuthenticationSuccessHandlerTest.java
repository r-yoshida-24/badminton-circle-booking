package com.badminton.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleBasedAuthenticationSuccessHandlerTest {

    private final RoleBasedAuthenticationSuccessHandler handler = new RoleBasedAuthenticationSuccessHandler();

    @Test
    void adminLoginRedirectsToAdminDashboard() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("loginType", "admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "admin", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/dashboard");
    }

    @Test
    void participantTryingAdminLoginIsRejectedToAdminLoginPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("loginType", "admin");
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "participant", "pw", List.of(new SimpleGrantedAuthority("ROLE_PARTICIPANT"))
        );

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("/admin/login?error=role");
    }
}
