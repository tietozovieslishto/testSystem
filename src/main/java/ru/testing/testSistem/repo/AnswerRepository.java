package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.testing.testSistem.models.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    boolean existsByQuestionIdAndCorrectTrue(Long questionId); // существует ли хотя бы 1 правильный ответ
}