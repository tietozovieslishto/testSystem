package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Question;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.TestAttempt;

@Data
@AllArgsConstructor
public class TestQuestionDto {
    private Test test;
    private TestAttempt attempt;
    private Question question;
    private int questionNumber;
    private int totalQuestions;
    private boolean timeExpired;

    public static TestQuestionDto timeExpired(TestAttempt attempt) {
        return new TestQuestionDto(
                attempt.getTest(),
                attempt,
                null,
                0,
                0,
                true
        );
    }
}