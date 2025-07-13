package com.example.backend;

import com.example.backend.model.User;
import com.example.backend.repository.SignupRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class BackendApplication {

    @Value("${stripe.api.key}")
    private String stripeKey; // Without static modifier

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    /*CommandLineRunner init(SignupRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Initializing users...");
            
            // Stripe key check
            try {
                System.out.println("Stripe key loaded: " + (stripeKey != null && !stripeKey.isEmpty() ? "YES" : "NO"));
            } catch (Exception e) {
                System.out.println("Stripe key check failed: " + e.getMessage());
            }

            if (userRepository.count() == 0) {
                // Create admin user
                User admin = new User();
                admin.setFullname("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRole("ROLE_ADMIN");
                admin.setAddress("Default Address");   
                admin.setBank("Default Bank Info");
                userRepository.save(admin);

                // Create regular user
                User user = new User();
                user.setFullname("Regular User");
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("userpass"));
                user.setRole("ROLE_USER");
                user.setAddress("Default Address");   
                user.setBank("Default Bank Info");
                userRepository.save(user);

                System.out.println("Users initialized");
            } else {
                System.out.println("Users already exist");
            }
        };
    } */

        CommandLineRunner init(SignupRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Initializing users...");
            
            if (userRepository.count() == 0) {
                // Create admin user
                User admin = new User();
                admin.setFullname("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRole("ROLE_ADMIN");
                admin.setAddress("Default Address");   
                admin.setBank("Default Bank Info");
                userRepository.save(admin);

                // Create regular user
                User user = new User();
                user.setFullname("Regular User");
                user.setEmail("user@example.com");
                user.setPassword(passwordEncoder.encode("userpass"));
                user.setRole("ROLE_USER");
                user.setAddress("Default Address");   
                user.setBank("Default Bank Info");
                userRepository.save(user);

                System.out.println("Users initialized");
            } else {
                System.out.println("Users already exist");
            }
        };
    }
}
