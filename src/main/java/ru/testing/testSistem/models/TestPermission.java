package ru.testing.testSistem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;  // связь с тестом

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //  кому дать доступ

    @Column(nullable = false)
    private boolean allowed = true; // разрешен ли доступ

    @CreationTimestamp
    private LocalDateTime createdAt;
}