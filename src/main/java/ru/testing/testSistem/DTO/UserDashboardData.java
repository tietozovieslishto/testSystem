package ru.testing.testSistem.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.testing.testSistem.models.TestAttempt;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserDashboardData {
    private String username;
    private List<TestAttempt> completedTests;
}