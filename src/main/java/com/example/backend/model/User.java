package com.example.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Entity
@Table(name = "users", 
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements UserDetails {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullname;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", 
            message = "Password must contain at least 1 uppercase, 1 lowercase, 1 number")
    @Column(nullable = false)
    private String password;
    
    @Pattern(regexp = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$", 
             message = "Invalid phone number format")
    private String phone;
    
    private String role = "ROLE_USER";
    
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    //@NotBlank(message = "Bank information is required")
    private String bank;
    
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Invalid USD amount")
    private String usd;
    
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Invalid ZIG amount")
    private String zig;
    
    //@NotBlank(message = "Address is required")
    //@Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    // Constructors
    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getUsd() { return usd; }
    public void setUsd(String usd) { this.usd = usd; }

    public String getZig() { return zig; }
    public void setZig(String zig) { this.zig = zig; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Formatted toString()
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", bank='" + bank + '\'' +
                ", usd='" + usd + '\'' +
                ", zig='" + zig + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Initialize default values
        if (this.usd == null || this.usd.isEmpty()) this.usd = "0.00";
        if (this.zig == null || this.zig.isEmpty()) this.zig = "0.00";
        if (this.address == null) this.address = "";
    }

    // Business logic methods
    public void initializeDefaultValues() {
        if (this.role == null) this.role = "ROLE_USER";
        if (this.usd == null) this.usd = "0.00";
        if (this.zig == null) this.zig = "0.00";
    }
}