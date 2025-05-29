package ru.testing.testSistem.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.DTO.TestQuestionDto;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestTakingService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final TestPermissionService permissionService;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public TestAttempt startTest(Long testId, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("тест не найден"));

        if (!permissionService.hasAccess(testId, user.getId())) {
            throw new AccessDeniedException("У вас нет доступа к этому тесту");
        }

        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus(TestAttemptStatus.IN_PROGRESS);
        attempt.setScore(0);

        return attemptRepository.save(attempt);
    }
   public void saveAnswer(Long attemptId, Long questionId, List<Long> answerIds, String textAnswer, User user) {
       TestAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, user.getId())
               .orElseThrow(() -> new EntityNotFoundException("попытка не найдена"));

       if (attempt.getStatus() != TestAttemptStatus.IN_PROGRESS) {
           throw new IllegalStateException("попытка завершена");
       }

       Question question = questionRepository.findById(questionId)
               .orElseThrow(() -> new EntityNotFoundException("вопрос не найден"));

       if (!question.getTest().getId().equals(attempt.getTest().getId())) {
           throw new IllegalArgumentException("Вопрос не относится к этому тесту");
       }

       userAnswerRepository.deleteByAttemptIdAndQuestionId(attemptId, questionId);

       boolean answerProvided = false; // ответ дан ?

       switch (question.getQuestionType()) {
           case SINGLE:
               if (answerIds != null && !answerIds.isEmpty()) {
                   saveSingleAnswer(attempt, question, answerIds.get(0));
                   answerProvided = true;
               }
               break;
           case MULTIPLE:
               if (answerIds != null && !answerIds.isEmpty()) {
                   saveMultipleAnswers(attempt, question, answerIds);
                   answerProvided = true;
               }
               break;
           case TEXT:
               if (textAnswer != null && !textAnswer.trim().isEmpty()) {
                   saveTextAnswer(attempt, question, textAnswer);
                   answerProvided = true;
               }
               break;
       }

       if (!answerProvided) {
           UserAnswer skippedAnswer = new UserAnswer();
           skippedAnswer.setAttempt(attempt);
           skippedAnswer.setQuestion(question);
           skippedAnswer.setAnswer(null);
           skippedAnswer.setUserTextAnswer(null);
           skippedAnswer.setCorrect(false);
           userAnswerRepository.save(skippedAnswer);
       }
   }
    private void saveSingleAnswer(TestAttempt attempt, Question question, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Ответ не найден"));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setAnswer(answer);
        userAnswer.setCorrect(answer.isCorrect());
        userAnswerRepository.save(userAnswer);

        if (answer.isCorrect()) {
            updateAttemptScore(attempt, question.getPoints());
        }
    }

    private void saveMultipleAnswers(TestAttempt attempt, Question question, List<Long> answerIds) {
        List<Answer> answers = answerRepository.findAllById(answerIds);

        boolean allCorrect = true;
        for (Answer answer : answers) {
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt);
            userAnswer.setQuestion(question);
            userAnswer.setAnswer(answer);
            userAnswer.setCorrect(answer.isCorrect());
            userAnswerRepository.save(userAnswer);

            if (!answer.isCorrect()) {
                allCorrect = false;
            }
        }

        // Для MULTIPLE вопросов все выбранные ответы должны быть правильными
        if (allCorrect && answers.size() == question.getAnswers().stream().filter(Answer::isCorrect).count()) {
            updateAttemptScore(attempt, question.getPoints());
        }
    }

    private void saveTextAnswer(TestAttempt attempt, Question question, String textAnswer) {
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setUserTextAnswer(textAnswer);

        boolean isCorrect = question.getAnswers().stream()
                .filter(Answer::isCorrect)               //оставляем только правильные
                .anyMatch(correctAnswer ->              // Проверяем совпадение хотя бы с одним
                        correctAnswer.getText().equalsIgnoreCase(textAnswer.trim()));           //trim()   - Удаляем пробелы в начале/конце ответа

        userAnswer.setCorrect(isCorrect);
        userAnswerRepository.save(userAnswer);

        if (isCorrect) {
            updateAttemptScore(attempt, question.getPoints());
        }
    }

    private void updateAttemptScore(TestAttempt attempt, int points) {
        attempt.setScore(attempt.getScore() + points);
        attemptRepository.save(attempt);
    }

    public TestAttempt completeTest(Long attemptId, User user) {
        TestAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("попытка не найдена"));

        if (attempt.getStatus() != TestAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Тест уже завершен");
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(TestAttemptStatus.COMPLETED);

        return attemptRepository.save(attempt);
    }

    public List<Question> getTestQuestions(Long testId) {
        return questionRepository.findByTestId(testId);
    }

    public TestAttempt getAttempt(Long attemptId, User user) {
        return attemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Попытка не найдена"));
    }


    public boolean isTimeExpired(TestAttempt attempt) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutTime = attempt.getStartTime().plusMinutes(attempt.getTest().getTimeLimitMinutes());
        return now.isAfter(timeoutTime); // false если не истекло
    }
    public List<Test> searchTests(String searchTerm) {
        // Сначала ищем по названию/описанию
        List<Test> results = testRepository.findByTitleOrDescription(searchTerm);

        // Пробуем как дату
        try {
            LocalDate date = LocalDate.parse(searchTerm);
            results.addAll(testRepository.findByDate(date));
        } catch (DateTimeParseException e) {
            // Если не получилось - просто игнорируем
        }

        // Удаляем дубликаты
        return results.stream().distinct().collect(Collectors.toList());        // distinct - удаляет дубликаты
    }
    public List<Test> getAvailableTests(String username, String search) {
        User user = getUserByUsername(username);

        if (search != null && !search.trim().isEmpty()) { // trim - удаляет пробелы
            return searchTests(search).stream()
                    .filter(Test::isActive)
                    .filter(test -> permissionService.hasAccess(test.getId(), user.getId()))
                    .collect(Collectors.toList());
        }

        return testRepository.findAll().stream()
                .filter(Test::isActive)
                .filter(test -> permissionService.hasAccess(test.getId(), user.getId()))
                .collect(Collectors.toList());
    }
    public TestQuestionDto getQuestionData(Long testId, Long attemptId, int questionNumber, String username) {
        User user = getUserByUsername(username);
        TestAttempt attempt = getAttempt(attemptId, user);

        if (isTimeExpired(attempt)) {
            completeTest(attemptId, user);
            return TestQuestionDto.timeExpired(attempt);
        }

        List<Question> questions = getTestQuestions(testId);
        validateQuestionNumber(questionNumber, questions.size());

        return new TestQuestionDto(
                attempt.getTest(),
                attempt,
                questions.get(questionNumber - 1),
                questionNumber,
                questions.size(),
                false
        );
    }
    public int saveAnswerAndGetNextQuestion(Long attemptId, Long testId, int questionNumber,
                                            List<Long> answerIds, String textAnswer, String username) {
        User user = getUserByUsername(username);
        TestAttempt attempt = getAttempt(attemptId, user);

        if (attempt.getStatus() == TestAttemptStatus.COMPLETED || attempt.getEndTime() != null) {
            return -1;
        }

        List<Question> questions = getTestQuestions(testId);
        Question currentQuestion = questions.get(questionNumber - 1); // индексация с 0

        saveAnswer(attemptId, currentQuestion.getId(), answerIds, textAnswer, user);

        return questionNumber < questions.size() ? questionNumber + 1 : -1;
    }
    public String getHomeUrl(UserDetails userDetails) {
        if (userDetails == null) {
            return "/auth/login";
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "/admin/dashboard";
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TESTER"))) {
            return "/tester/dashboard";
        }
        return "/user/dashboard";
    }
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private void validateQuestionNumber(int questionNumber, int totalQuestions) {
        if (questionNumber < 1 || questionNumber > totalQuestions) {
            throw new IllegalArgumentException("Invalid question number");
        }
    }
}