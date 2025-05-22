package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.TestAttempt;

import java.util.List;

@Data
@AllArgsConstructor
public class TesterDashboardDto {
    private String username;
    private List<Test> createdTests;
    private List<TestAttempt> completedTests;
}