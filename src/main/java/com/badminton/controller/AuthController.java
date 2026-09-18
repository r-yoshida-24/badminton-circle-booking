package com.badminton.controller;

import com.badminton.dto.LineAuthRequest;
import com.badminton.dto.LineAuthResponse;
import com.badminton.exception.LineAuthenticationException;
import com.badminton.exception.MemberDisabledException;
import com.badminton.exception.MemberNotFoundException;
import com.badminton.security.MemberPrincipal;
import com.badminton.service.AuthenticatedLineMember;
import com.badminton.service.LineAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    private final LineAuthService lineAuthService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            LineAuthService lineAuthService,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            SecurityContextRepository securityContextRepository
    ) {
        this.lineAuthService = lineAuthService;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping(value = "/auth/line", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<LineAuthResponse> authenticateWithLine(
            @Valid @RequestBody LineAuthRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        try {
            AuthenticatedLineMember authenticatedMember = lineAuthService.authenticate(request);
            MemberPrincipal principal = authenticatedMember.principal();
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            sessionAuthenticationStrategy.onAuthentication(authentication, httpServletRequest, httpServletResponse);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

            return ResponseEntity.ok(new LineAuthResponse(true, "/attendance", null));
        } catch (MemberNotFoundException ex) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new LineAuthResponse(false, "/auth/unregistered", ex.getMessage()));
        } catch (MemberDisabledException ex) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new LineAuthResponse(false, "/auth/disabled", ex.getMessage()));
        } catch (LineAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LineAuthResponse(false, "/auth/error", ex.getMessage()));
        }
    }

    @GetMapping("/auth/unregistered")
    public String unregistered() {
        return "auth/unregistered";
    }

    @GetMapping("/auth/disabled")
    public String disabled() {
        return "auth/disabled";
    }

    @GetMapping("/auth/error")
    public String error() {
        return "auth/error";
    }
}
