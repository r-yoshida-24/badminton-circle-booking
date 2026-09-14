package com.badminton.controller;

import com.badminton.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/participant/register")
    public String registerParticipant(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String email,
                                      @RequestParam String displayName) {
        authService.registerParticipant(username, password, email, displayName);
        return "redirect:/participant/login?registered";
    }

    @PostMapping("/admin/register")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String email,
                                @RequestParam String displayName) {
        authService.registerAdmin(username, password, email, displayName);
        return "redirect:/admin/login?registered";
    }
}
