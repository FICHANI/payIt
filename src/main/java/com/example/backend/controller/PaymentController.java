package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.model.Transaction;
import com.example.backend.repository.SignupRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class PaymentController {
    private final StripeService stripeService;
    private final SignupRepository userRepository;
    private final TransactionRepository transactionRepo;
    private final CsrfTokenRepository csrfTokenRepository;

    @PostMapping(
        value = "/create-payment-intent",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createPaymentIntent(
            @Valid @RequestBody PaymentRequest request,
            Authentication auth,
            @RequestHeader(value = "X-XSRF-TOKEN", required = false) String xsrfToken) {

        if (xsrfToken == null || !csrfTokenRepository.loadToken(null).getToken().equals(xsrfToken)) {
            log.warn("Invalid CSRF token for user: {}", auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid CSRF token"));
        }

        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> {
                        log.error("User not found: {}", auth.getName());
                        return new RuntimeException("User not found");
                    });

            if (user.getStripeCustomerId() == null) {
                String customerId = stripeService.createCustomer(user.getEmail(), user.getFullname());
                user.setStripeCustomerId(customerId);
                userRepository.save(user);
                log.info("Created Stripe customer for user: {}", user.getEmail());
            }

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (request.getAmount() * 100))
                .setCurrency(request.getCurrency().toLowerCase())
                .setCustomer(user.getStripeCustomerId())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build()
                )
                .setDescription(request.getDescription())
                .setReceiptEmail(request.getRecipientEmail())
                .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Created PaymentIntent {} for user {}", intent.getId(), user.getEmail());

            return ResponseEntity.ok(Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId(),
                "amount", request.getAmount(),
                "currency", request.getCurrency(),
                "status", "requires_payment_method"
            ));

        } catch (StripeException e) {
            log.error("Stripe payment failed for user {}: {}", auth.getName(), e.getMessage());
            return handleStripeException(e);
        }
    }

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(
            @RequestParam @NotBlank String recipientEmail,
            @RequestParam @Positive double amount,
            @RequestParam @NotBlank @Size(min = 3, max = 3) String currency,
            Authentication auth) {

        try {
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            PaymentIntent intent = stripeService.createPaymentIntent(
                (long) (amount * 100),
                currency.toLowerCase(),
                user.getStripeCustomerId(),
                "pm_card_visa"
            );

            Transaction transaction = Transaction.builder()
                .user(user)
                .stripePaymentId("pi_1234567890") 
                .recipientEmail(recipientEmail)
                .amount(amount)
                .currency(currency)
                .status("succeeded")
                .timestamp(LocalDateTime.now())
                .build();

            transactionRepo.save(transaction);
            log.info("Processed payment {} for user {}", intent.getId(), user.getEmail());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "transactionId", transaction.getId()
            ));
            
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Payment failed: " + e.getMessage()));
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:8080", "https://your-frontend.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }

    private ResponseEntity<Map<String, Object>> handleStripeException(StripeException e) {
        HttpStatus status = e.getStatusCode() == 402 ? 
            HttpStatus.PAYMENT_REQUIRED : 
            HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status)
            .body(Map.of(
                "error", e.getMessage(),
                "code", e.getCode(),
                "timestamp", LocalDateTime.now()
            ));
    }
  
    @Data
    public static class PaymentRequest {
        @NotBlank
        private String paymentMethodId;

        @Positive
        private double amount;

        @NotBlank @Size(min = 3, max = 3)
        private String currency;

        private String description;

        @NotBlank @Email
        private String recipientEmail;
    }
}