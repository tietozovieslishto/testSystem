package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Question;
import ru.testing.testSistem.models.TestAttempt;
import ru.testing.testSistem.models.UserAnswer;

import java.util.List;
import java.util.Map;
//
@Data
@AllArgsConstructor
public class TestResultDto {
    private TestAttempt attempt;
    private List<UserAnswer> userAnswers;
    private List<Question> questions;
    private Map<Long, String> correctTextAnswers;
    private int totalQuestions;
    private Map<Long, Boolean> questionCorrectness;
}