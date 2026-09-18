package com.badminton.service;

import com.badminton.config.LineProperties;
import com.badminton.dto.LineAuthRequest;
import com.badminton.entity.Member;
import com.badminton.entity.Role;
import com.badminton.exception.LineAuthenticationException;
import com.badminton.exception.MemberDisabledException;
import com.badminton.exception.MemberNotFoundException;
import com.badminton.security.LineIdTokenClaims;
import com.badminton.security.LineIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LineAuthServiceTest {

    @Mock
    private LineIdTokenVerifier lineIdTokenVerifier;
    @Mock
    private MemberService memberService;

    private LineAuthService lineAuthService;

    @BeforeEach
    void setUp() {
        LineProperties lineProperties = new LineProperties();
        lineProperties.setFriendshipCheckEnabled(true);
        lineAuthService = new LineAuthService(lineIdTokenVerifier, memberService, lineProperties);
    }

    @Test
    void authenticateSucceedsWhenTokenAndMemberAreValid() {
        Member member = member("U123", true, Role.USER);
        when(lineIdTokenVerifier.verify("valid")).thenReturn(new LineIdTokenClaims("U123", "会員A"));
        when(memberService.getEnabledMemberByLineUserId("U123")).thenReturn(member);

        AuthenticatedLineMember result = lineAuthService.authenticate(new LineAuthRequest("valid", true));

        assertThat(result.principal().getLineUserId()).isEqualTo("U123");
        verify(memberService).syncDisplayName(member, "会員A");
    }

    @Test
    void authenticateFailsWhenVerifierRejectsInvalidToken() {
        doThrow(new LineAuthenticationException("invalid")).when(lineIdTokenVerifier).verify(anyString());

        assertThatThrownBy(() -> lineAuthService.authenticate(new LineAuthRequest("invalid", true)))
                .isInstanceOf(LineAuthenticationException.class);
    }

    @Test
    void authenticateFailsWhenFriendshipCheckIsRequiredButFalse() {
        assertThatThrownBy(() -> lineAuthService.authenticate(new LineAuthRequest("valid", false)))
                .isInstanceOf(LineAuthenticationException.class)
                .hasMessageContaining("友だち追加");
    }

    @Test
    void authenticateFailsWhenMemberNotRegistered() {
        when(lineIdTokenVerifier.verify("valid")).thenReturn(new LineIdTokenClaims("U404", "会員A"));
        when(memberService.getEnabledMemberByLineUserId("U404")).thenThrow(new MemberNotFoundException());

        assertThatThrownBy(() -> lineAuthService.authenticate(new LineAuthRequest("valid", true)))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void authenticateFailsWhenMemberDisabled() {
        when(lineIdTokenVerifier.verify("valid")).thenReturn(new LineIdTokenClaims("U999", "会員A"));
        when(memberService.getEnabledMemberByLineUserId("U999")).thenThrow(new MemberDisabledException());

        assertThatThrownBy(() -> lineAuthService.authenticate(new LineAuthRequest("valid", true)))
                .isInstanceOf(MemberDisabledException.class);
    }

    private Member member(String lineUserId, boolean enabled, Role role) {
        Member member = new Member();
        member.setLineUserId(lineUserId);
        member.setDisplayName("既存名");
        member.setEnabled(enabled);
        member.setRole(role);
        return member;
    }
}
