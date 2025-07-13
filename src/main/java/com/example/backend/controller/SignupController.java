package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.SignupRepository;
import com.example.backend.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
public class SignupController {
    @Autowired
    private SignupRepository signupRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private StripeService stripeService;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @ModelAttribute("user") User user,
            @RequestParam("pass1") String pass1,
            @RequestParam("pass2") String pass2,
            Model model) {

        // Validate passwords
        if (pass1.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters");
            return "signup";
        }

        if (pass1 == null || pass2 == null || pass1.isBlank() || pass2.isBlank()) {
            model.addAttribute("error", "Password fields cannot be empty.");
            return "signup";
        }

        if (!pass1.equals(pass2)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }

        if (signupRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email is already registered.");
            return "signup";
        }

        // Set default values
        if(user.getUsd() == null) user.setUsd("0");
        if(user.getZig() == null) user.setZig("0");
        
        // Create Stripe customer
        try {
            String stripeCustomerId = stripeService.createCustomer(user.getEmail(), user.getFullname());
            user.setStripeCustomerId(stripeCustomerId);
        } catch (StripeException e) {
            model.addAttribute("error", "Payment service unavailable. Please try again later.");
            return "signup";
        }
        
        // Save user
        user.setPassword(passwordEncoder.encode(pass1));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole("ROLE_USER"); // Set default role
        signupRepository.save(user);

        return "redirect:/login?registered=true";
    }
}