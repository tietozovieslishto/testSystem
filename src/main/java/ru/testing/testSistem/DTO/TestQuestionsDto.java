package ru.testing.testSistem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.testing.testSistem.models.Question;
import ru.testing.testSistem.models.Test;

import java.util.List;
//
@Data
@AllArgsConstructor
public class TestQuestionsDto {
    private Test test;
    private List<Question> questions;
    private QuestionForm questionForm;
    private AnswerForm answerForm;
}