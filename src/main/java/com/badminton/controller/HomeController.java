package com.badminton.controller;

import com.badminton.config.LineProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LineProperties lineProperties;

    public HomeController(LineProperties lineProperties) {
        this.lineProperties = lineProperties;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/attendance";
        }
        return "redirect:/liff-login";
    }

    @GetMapping("/liff-login")
    public String liffLogin(Model model) {
        model.addAttribute("liffId", lineProperties.getLiffId());
        model.addAttribute("friendshipCheckEnabled", lineProperties.isFriendshipCheckEnabled());
        return "auth/liff-login";
    }
}
