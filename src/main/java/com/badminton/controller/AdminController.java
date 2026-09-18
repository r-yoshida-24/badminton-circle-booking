package com.badminton.controller;

import com.badminton.form.EventForm;
import com.badminton.service.AttendanceService;
import com.badminton.service.EventService;
import com.badminton.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EventService eventService;
    private final MemberService memberService;
    private final AttendanceService attendanceService;

    public AdminController(EventService eventService, MemberService memberService, AttendanceService attendanceService) {
        this.eventService = eventService;
        this.memberService = memberService;
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/events";
    }

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.findAllEvents());
        return "admin/events/index";
    }

    @GetMapping("/events/new")
    public String newEvent(Model model) {
        model.addAttribute("eventForm", new EventForm());
        model.addAttribute("formAction", "/admin/events");
        model.addAttribute("pageTitle", "イベント登録");
        return "admin/events/form";
    }

    @PostMapping("/events")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/events");
            model.addAttribute("pageTitle", "イベント登録");
            return "admin/events/form";
        }
        eventService.createEvent(form);
        redirectAttributes.addFlashAttribute("successMessage", "イベントを登録しました。");
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{eventId}/edit")
    public String editEvent(@PathVariable Long eventId, Model model) {
        model.addAttribute("eventForm", EventForm.from(eventService.getEvent(eventId)));
        model.addAttribute("formAction", "/admin/events/" + eventId);
        model.addAttribute("pageTitle", "イベント編集");
        return "admin/events/form";
    }

    @PostMapping("/events/{eventId}")
    public String updateEvent(@PathVariable Long eventId, @Valid @ModelAttribute("eventForm") EventForm form, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/events/" + eventId);
            model.addAttribute("pageTitle", "イベント編集");
            return "admin/events/form";
        }
        eventService.updateEvent(eventId, form);
        redirectAttributes.addFlashAttribute("successMessage", "イベントを更新しました。");
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable Long eventId, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(eventId);
        redirectAttributes.addFlashAttribute("successMessage", "イベントを削除しました。");
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{eventId}/participants")
    public String participants(@PathVariable Long eventId, Model model) {
        model.addAttribute("event", eventService.getEvent(eventId));
        model.addAttribute("participants", attendanceService.getEventParticipants(eventId));
        return "admin/events/participants";
    }

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberService.findAllMembers());
        return "admin/members/index";
    }
}
