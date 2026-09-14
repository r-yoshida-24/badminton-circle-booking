package com.badminton.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String loginType = request.getParameter("loginType");
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isParticipant = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_PARTICIPANT".equals(a.getAuthority()));

        if ("admin".equals(loginType)) {
            if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } else {
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                response.sendRedirect("/admin/login?error=role");
            }
            return;
        }

        if ("participant".equals(loginType)) {
            if (isParticipant) {
                response.sendRedirect("/participant/dashboard");
            } else {
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }
                response.sendRedirect("/participant/login?error=role");
            }
            return;
        }

        if (isAdmin) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/participant/dashboard");
        }
    }
}
