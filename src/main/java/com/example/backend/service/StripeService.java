package com.example.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import com.example.backend.config.StripeConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);
    private final StripeConfig stripeConfig;

    public StripeService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeConfig.getSecretKey();
        Stripe.setMaxNetworkRetries(2);
    }

    // Customer Management
    public String createCustomer(String email, String name) throws StripeException {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .build();

            Customer customer = Customer.create(params);
            logger.info("Created customer: {}", customer.getId());
            return customer.getId();
        } catch (StripeException e) {
            logger.error("Failed to create customer: {}", e.getMessage());
            throw new StripeServiceException("Customer creation failed", e);
        }
    }

    // Payment Intents
    public PaymentIntent createPaymentIntent(long amount, String currency, 
                                          String customerId, String paymentMethodId) 
                                          throws StripeException {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency.toLowerCase())
                    .setCustomer(customerId)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            logger.info("Created payment intent: {}", intent.getId());
            return intent;
        } catch (StripeException e) {
            logger.error("Payment intent creation failed: {}", e.getMessage());
            throw new StripeServiceException("Payment processing failed", e);
        }
    }

    // Payment Methods
    public PaymentMethod attachPaymentMethod(String paymentMethodId, String customerId) 
            throws StripeException {
        try {
            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();

            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            return paymentMethod.attach(params);
        } catch (StripeException e) {
            logger.error("Failed to attach payment method: {}", e.getMessage());
            throw new StripeServiceException("Payment method attachment failed", e);
        }
    }

    public static class StripeServiceException extends RuntimeException {
        public StripeServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}