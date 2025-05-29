package ru.testing.testSistem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank // не пустое
    @Email // проверяет формат маил
    @Column(nullable = false, unique = true, length = 255) // должен быть уникальным
    private String email;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String password;
    private boolean enabled = true;
    private boolean emailVerified = true;
    private String verificationToken;
    @Column(columnDefinition = "TIMESTAMP") // столбец должен быть типа TIMESTAMP
    private LocalDateTime tokenExpiry; // срок действия токена, нужен для дальнейшего расширения
    @CreationTimestamp // авто установление даты
    private LocalDateTime createdAt;
    @ManyToMany(fetch = FetchType.EAGER) // жадная загрузка
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),  // joinColumns - столбец, ссылающийся на текущую сущность
            inverseJoinColumns = @JoinColumn(name = "role_id")) //столбец, ссылающийся на связанную сущность
    private Set<Role> roles = new HashSet<>(); // без дубликатов

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}