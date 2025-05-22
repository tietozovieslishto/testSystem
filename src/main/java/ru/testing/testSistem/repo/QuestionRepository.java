package ru.testing.testSistem.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.testing.testSistem.models.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTestId(Long testId);
}