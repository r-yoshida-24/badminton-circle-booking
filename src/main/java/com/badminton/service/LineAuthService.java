package com.badminton.service;

import com.badminton.config.LineProperties;
import com.badminton.entity.Member;
import com.badminton.security.LineIdTokenClaims;
import com.badminton.security.LineIdTokenVerifier;
import com.badminton.security.MemberPrincipal;
import com.badminton.dto.LineAuthRequest;
import com.badminton.exception.LineAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineAuthService {

    private final LineIdTokenVerifier lineIdTokenVerifier;
    private final MemberService memberService;
    private final LineProperties lineProperties;

    public LineAuthService(LineIdTokenVerifier lineIdTokenVerifier, MemberService memberService, LineProperties lineProperties) {
        this.lineIdTokenVerifier = lineIdTokenVerifier;
        this.memberService = memberService;
        this.lineProperties = lineProperties;
    }

    @Transactional
    public AuthenticatedLineMember authenticate(LineAuthRequest request) {
        if (lineProperties.isFriendshipCheckEnabled() && !Boolean.TRUE.equals(request.friendshipChecked())) {
            throw new LineAuthenticationException("公式LINEアカウントを友だち追加した後にご利用ください。");
        }

        LineIdTokenClaims claims = lineIdTokenVerifier.verify(request.idToken());
        Member member = memberService.getEnabledMemberByLineUserId(claims.subject());
        memberService.syncDisplayName(member, claims.displayName());

        MemberPrincipal principal = new MemberPrincipal(
                member.getId(),
                member.getLineUserId(),
                member.getDisplayName(),
                member.getRole(),
                member.isEnabled()
        );
        return new AuthenticatedLineMember(principal);
    }
}
