package ru.testing.testSistem.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_attempts") // попытки прохождения
@Data
@NoArgsConstructor
public class TestAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // кто проходил

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer score; // результат

    @Enumerated(EnumType.STRING)
    private TestAttemptStatus status;
    public Test getTest() {
        return this.test;
    }
}