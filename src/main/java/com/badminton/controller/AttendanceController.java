package com.badminton.controller;

import com.badminton.form.AttendanceForm;
import com.badminton.security.MemberPrincipal;
import com.badminton.service.AttendanceService;
import com.badminton.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    public AttendanceController(AttendanceService attendanceService, MemberService memberService) {
        this.attendanceService = attendanceService;
        this.memberService = memberService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal MemberPrincipal principal, Model model) {
        model.addAttribute("currentMember", memberService.getCurrentMember(principal));
        model.addAttribute("attendanceEvents", attendanceService.getUpcomingAttendanceViews(principal));
        return "attendance/index";
    }

    @PostMapping("/{eventId}")
    public String updateAttendance(
            @PathVariable Long eventId,
            @Valid AttendanceForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal MemberPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:/attendance";
        }

        attendanceService.updateMyAttendance(eventId, form.getStatus(), principal);
        redirectAttributes.addFlashAttribute("successMessage", "出欠を更新しました。");
        return "redirect:/attendance";
    }
}
