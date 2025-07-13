package com.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

/*@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        ServletWebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorDetails = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        model.addAttribute("timestamp", errorDetails.get("timestamp"));
        model.addAttribute("status", errorDetails.get("status"));
        model.addAttribute("error", errorDetails.get("error"));
        model.addAttribute("message", errorDetails.get("message"));
        model.addAttribute("path", errorDetails.get("path"));

        return "error"; // refers to error.html in /templates
    }
}
*/

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;
    
    @Autowired
    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Skip API errors (let them be handled by @RestControllerAdvice)
        if (request.getRequestURI().startsWith("/api/")) {
            return null; // Delegate to Spring's default error handling
        }

        // Handle web errors (return HTML)
        ServletWebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorDetails = errorAttributes.getErrorAttributes(
            webRequest, 
            ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );

        model.addAttribute("error", errorDetails);
        return "error"; // Renders error.html
    }
}