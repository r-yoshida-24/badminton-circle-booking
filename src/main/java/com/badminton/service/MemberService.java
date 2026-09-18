package com.badminton.service;

import com.badminton.entity.Member;
import com.badminton.exception.MemberDisabledException;
import com.badminton.exception.MemberNotFoundException;
import com.badminton.repository.MemberRepository;
import com.badminton.security.MemberPrincipal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member getEnabledMemberByLineUserId(String lineUserId) {
        Member member = memberRepository.findByLineUserId(lineUserId)
                .orElseThrow(MemberNotFoundException::new);
        if (!member.isEnabled()) {
            throw new MemberDisabledException();
        }
        return member;
    }

    public Member getCurrentMember(MemberPrincipal principal) {
        return memberRepository.findById(principal.getMemberId())
                .orElseThrow(MemberNotFoundException::new);
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll(Sort.by(Sort.Order.asc("displayName"), Sort.Order.asc("id")));
    }

    @Transactional
    public void syncDisplayName(Member member, String displayName) {
        if (StringUtils.hasText(displayName) && !displayName.equals(member.getDisplayName())) {
            member.setDisplayName(displayName);
        }
    }
}
