package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.SignupRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    private final SignupRepository signupRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return signupRepository.findByEmail(authentication.getName()).orElse(null);
    }

    public LoginController(SignupRepository signupRepository) {
        this.signupRepository = signupRepository;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        // Necessary model attributes for the login page
        return "login";
    }

    // This method handles successful logins via Spring Security's form login
    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication, HttpSession session) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) return "redirect:/login?error=invalid_session";

        session.setAttribute("user", user);
        return switch (user.getRole()) {
            case "ROLE_ADMIN" -> "redirect:/admin";
            case "ROLE_USER" -> "redirect:/home";
            default -> "redirect:/login?error=invalid_role";
        };
    }

    @GetMapping("/home")
    public String userHome(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = signupRepository.findByEmail(auth.getName()).orElse(null);
        
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "home";
    }

    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }
}