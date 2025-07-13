package com.example.backend.controller;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HistoryController {

    @PostMapping("/display-history")
    public String processPayment(
            @RequestParam String recipient,
            @RequestParam double amount,
            Model model
    ) {
         // Create mock API endpoint
        String apiUrl = "https://jsonplaceholder.typicode.com/posts"; // Fake endpoint

        // Build request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("recipient", recipient);
        requestBody.put("amount", amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Send POST request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("recipient", recipient);
                model.addAttribute("amount", amount);
                model.addAttribute("timestamp", LocalDateTime.now());
                return "admin"; // Show receipt
            } else {
                model.addAttribute("error", "⚠️ Payment failed with status: " + response.getStatusCode());
                return "admin"; // Show error on home
            }
        } catch (Exception e) {
            model.addAttribute("error", " Could not connect to payment API: " + e.getMessage());
            return "admin"; // Show error on home
        }
    }
}
