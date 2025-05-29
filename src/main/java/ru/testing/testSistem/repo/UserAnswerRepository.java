package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import ru.testing.testSistem.models.UserAnswer;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByAttemptId(Long attemptId);       // Поиск ответов по ID попытки
    @Modifying // изменяет данные
    void deleteByAttemptIdAndQuestionId(Long attemptId, Long questionId);       //Удаляет конкретный ответ

}