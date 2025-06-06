package ru.testing.testSistem.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.testing.testSistem.DTO.TestResultDto;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.QuestionRepository;
import ru.testing.testSistem.repo.TestAttemptRepository;
import ru.testing.testSistem.repo.UserAnswerRepository;
import ru.testing.testSistem.repo.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestResultService {
    private final TestAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(TestResultService.class);

    public TestResultDto getTestResult(Long testId, Long attemptId, String username) {
        User user = getUserByUsername(username);
        TestAttempt attempt = getAttempt(attemptId, user);

        try {
            emailService.sendTestResultsEmail(user, attempt);
        } catch (Exception e) {
            logger.error("Failed to send results email", e);
        }

        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attemptId);
        List<Question> questions = questionRepository.findByTestId(testId);

        Map<Long, Boolean> questionCorrectness = calculateQuestionCorrectness(questions, userAnswers);      //правильность каждого ответа
        Map<Long, String> correctTextAnswers = getCorrectTextAnswers(questions);        // Нужен для показа правильных ответов

        return new TestResultDto(
                attempt,
                userAnswers,
                questions,
                correctTextAnswers,
                questions.size(),
                questionCorrectness
        );
    }

    private Map<Long, Boolean> calculateQuestionCorrectness(List<Question> questions, List<UserAnswer> userAnswers) {           // анализ правильности ответов
        Map<Long, Boolean> correctnessMap = new HashMap<>();

        for (Question question : questions) {
            List<UserAnswer> questionUserAnswers = userAnswers.stream()         //берет все ответы пользователей и оставляет только относящиеся к этому вопросу
                    .filter(ua -> ua.getQuestion().getId().equals(question.getId()))
                    .collect(Collectors.toList());

            boolean isCorrect = false;

            switch (question.getQuestionType()) {
                case SINGLE:
                    isCorrect = questionUserAnswers.stream()
                            .findFirst()
                            .map(UserAnswer::isCorrect)
                            .orElse(false);     // если ответа нет
                    break;

                case MULTIPLE:
                    List<Answer> correctAnswers = question.getAnswers().stream()        // все правильные ответы на вопрос
                            .filter(Answer::isCorrect)
                            .collect(Collectors.toList());

                    List<Answer> userSelectedAnswers = questionUserAnswers.stream()         // Ответы, выбранные пользователем
                            .map(UserAnswer::getAnswer)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    boolean allSelectedCorrect = userSelectedAnswers.stream()           // Все выбранные ответы правильные?
                            .allMatch(Answer::isCorrect);

                    boolean allCorrectSelected = correctAnswers.stream()             // Выбраны все правильные ответы?
                            .allMatch(ca -> userSelectedAnswers.stream()                // проверяет что все элементы удовлетворяют условию
                                    .anyMatch(ua -> ua.getId().equals(ca.getId())));            //Есть ли хотя бы один (anyMatch) выбранный пользователем ответ (ua), ID которого совпадает с ID этого правильного ответа?

                    isCorrect = allSelectedCorrect && allCorrectSelected;
                    break;

                case TEXT:
                    isCorrect = questionUserAnswers.stream()
                            .findFirst()
                            .map(UserAnswer::isCorrect)
                            .orElse(false);
                    break;
            }

            correctnessMap.put(question.getId(), isCorrect);
        }

        return correctnessMap;
    }


    private Map<Long, String> getCorrectTextAnswers(List<Question> questions) {
        return questions.stream()
                .filter(q -> q.getQuestionType() == QuestionType.TEXT)
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> q.getAnswers().stream()        //для каждого вопроса получаем поток его ответов
                                .filter(Answer::isCorrect)
                                .map(Answer::getText)
                                .collect(Collectors.joining(", "))      // объединяем в строку
                ));
    }

    public TestAttempt getAttempt(Long attemptId, User user) {
        return attemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found"));
    }
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }


}