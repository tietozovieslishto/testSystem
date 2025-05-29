package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Test;
//
@Data
@AllArgsConstructor
public class TestResultsDto {
    private Test test;
    private int attemptsCount;
    private double averageScore;
    private int maxPossibleScore;
}