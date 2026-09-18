package com.badminton.security;

import com.badminton.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberPrincipal implements UserDetails {

    private final Long memberId;
    private final String lineUserId;
    private final String displayName;
    private final Role role;
    private final boolean enabled;

    public MemberPrincipal(Long memberId, String lineUserId, String displayName, Role role, boolean enabled) {
        this.memberId = memberId;
        this.lineUserId = lineUserId;
        this.displayName = displayName;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return lineUserId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
