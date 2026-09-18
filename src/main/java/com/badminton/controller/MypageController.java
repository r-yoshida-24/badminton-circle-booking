package com.badminton.controller;

import com.badminton.security.MemberPrincipal;
import com.badminton.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MemberService memberService;

    public MypageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        model.addAttribute("member", memberService.getCurrentMember(principal));
        return "mypage/index";
    }
}
