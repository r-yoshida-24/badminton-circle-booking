package com.badminton.service;

import com.badminton.security.MemberPrincipal;

public record AuthenticatedLineMember(MemberPrincipal principal) {
}
