package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.TestAttempt;
import ru.testing.testSistem.models.TestAttemptStatus;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    Optional<TestAttempt> findByIdAndUserId(Long attemptId, Long userId);
    List<TestAttempt> findByUserIdAndStatusOrderByEndTimeDesc(Long userId, TestAttemptStatus status);
    List<TestAttempt> findByTestId(Long testId);
}