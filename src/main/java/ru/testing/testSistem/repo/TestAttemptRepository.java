package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.TestAttempt;
import ru.testing.testSistem.models.TestAttemptStatus;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    Optional<TestAttempt> findByIdAndUserId(Long attemptId, Long userId); // ищет по id попытки и пользователя
    List<TestAttempt> findByUserIdAndStatusOrderByEndTimeDesc(Long userId, TestAttemptStatus status); // ищет все  попытки пользователя с статусом + фильтрация
    List<TestAttempt> findByTestId(Long testId);
}