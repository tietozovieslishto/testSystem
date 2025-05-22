package ru.testing.testSistem.services;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.DTO.AdminDashboardDto;
import ru.testing.testSistem.DTO.UserDashboardData;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.TestAttemptRepository;
import ru.testing.testSistem.repo.TestRepository;
import ru.testing.testSistem.repo.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDashboardService {
    private final UserRepository userRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final TestRepository testRepository;

    public UserDashboardData getUserDashboardData(String username) {
        User user = getUserByUsername(username);
        List<TestAttempt> completedTests = getCompletedTestsWithMaxScore(user.getId());
        return new UserDashboardData(user.getUsername(), completedTests);
    }

    public AdminDashboardDto getAdminDashboard(String username) {
        User admin = getUserByUsername(username);
        List<Test> createdTests = testRepository.findByAuthorId(admin.getId());
        List<TestAttempt> completedTests = testAttemptRepository
                .findByUserIdAndStatusOrderByEndTimeDesc(admin.getId(), TestAttemptStatus.COMPLETED)
                .stream()
                .map(this::enrichWithMaxScore)
                .collect(Collectors.toList());
        return new AdminDashboardDto(admin.getUsername(), createdTests, completedTests);
    }

    private List<TestAttempt> getCompletedTestsWithMaxScore(Long userId) {
        return testAttemptRepository.findByUserIdAndStatusOrderByEndTimeDesc(
                        userId,
                        TestAttemptStatus.COMPLETED
                ).stream().map(this::calculateMaxScoreForAttempt)
                .collect(Collectors.toList());
    }

    private TestAttempt calculateMaxScoreForAttempt(TestAttempt attempt) {
        return enrichWithMaxScore(attempt);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private TestAttempt enrichWithMaxScore(TestAttempt attempt) {
        int maxScore = attempt.getTest().getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();
        attempt.getTest().setMaxScore(maxScore);
        return attempt;
    }
}