package ru.testing.testSistem.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.testing.testSistem.DTO.*;
import ru.testing.testSistem.exception.ResourceNotFoundException;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TesterService {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestCreationService testCreationService;
    private final TestPermissionService testPermissionService;

    public TesterDashboardDto getTesterDashboard(String username) {
        User tester = getUserByUsername(username);

        List<Test> createdTests = testRepository.findByAuthorId(tester.getId());

        List<TestAttempt> completedTests = testAttemptRepository
                .findByUserIdAndStatusOrderByEndTimeDesc(tester.getId(), TestAttemptStatus.COMPLETED)
                .stream()
                .map(this::enrichWithMaxScore)
                .collect(Collectors.toList()); // собираем результат

        return new TesterDashboardDto(tester.getUsername(), createdTests, completedTests);
    }

    public Test createTest(TestForm form, String username) {
        User author = getUserByUsername(username);
        return testCreationService.createTest(form, author);
    }

    public TestQuestionsDto getTestQuestionsData(Long testId, String username) throws AccessDeniedException { // данные для отображения страницы редактирования вопросов теста
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        return new TestQuestionsDto(
                test,
                testCreationService.getTestQuestions(testId),
                new QuestionForm(),
                new AnswerForm()
        );
    }

    public void addQuestion(Long testId, QuestionForm form, MultipartFile image, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);
        testCreationService.addQuestionToTest(testId, form, image);
    }


    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void checkEditPermission(Long userId, Long testId) throws AccessDeniedException {
        if (!testPermissionService.canEditTest(userId, testId)) {
            throw new AccessDeniedException("No permission to edit this test");
        }
    }

    private TestAttempt enrichWithMaxScore(TestAttempt attempt) {
        int maxScore = attempt.getTest().getQuestions().stream()
                .mapToInt(Question::getPoints) // преобразует каждый объект потока в int
                .sum();
        attempt.getTest().setMaxScore(maxScore);
        return attempt;
    }
    public void toggleTestPrivacy(Long testId, String username) throws AccessDeniedException { // сменить приватность
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        test.setPrivate(!test.isPrivate());
        testRepository.save(test);
    }
    public void toggleTestStatus(Long testId, String username) throws AccessDeniedException { // сменить активность
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        test.setActive(!test.isActive());
        testRepository.save(test);
    }
    public Test getTestForInvitation(Long testId, String username) throws AccessDeniedException { // получить тест для отправки приглашения
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        return testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
    }
    public String getHomeUrl(UserDetails userDetails) { // в help
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "/admin/dashboard";
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TESTER"))) {
            return "/tester/dashboard";
        }
        return "/user/dashboard";
    }
    public void deleteQuestion(Long testId, Long questionId, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        questionRepository.deleteById(questionId);
    }
    public void addAnswer(Long testId, Long questionId, AnswerForm answerForm, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getQuestionType() == QuestionType.SINGLE && answerForm.isCorrect()) { // и
            boolean hasCorrectAnswer = answerRepository.existsByQuestionIdAndCorrectTrue(questionId);
            if (hasCorrectAnswer) {
                throw new IllegalArgumentException(
                        "Для вопроса с одним правильным ответом можно отметить только один правильный вариант");
            }
        }

        testCreationService.addAnswerToQuestion(questionId, answerForm);
    }
    public void deleteAnswer(Long testId, Long answerId, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        answerRepository.deleteById(answerId);
    }
    public TestPermissionsDto getTestPermissions(Long testId, String search, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        checkEditPermission(user.getId(), testId);

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        List<User> usersToAdd = search != null ? // если search есть
                testPermissionService.searchUsersToAdd(testId, search) : // то это
                List.of(); // пустой список

        return new TestPermissionsDto(
                test,
                usersToAdd,
                testPermissionService.getAllowedUsers(testId)
        );
    }
    public TestResultsDto getTestResults(Long testId, String username) throws AccessDeniedException {
        User user = getUserByUsername(username);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (!test.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You don't have permission to view these results");
        }

        List<TestAttempt> attempts = testAttemptRepository.findByTestId(testId);
        int attemptsCount = attempts.size();

        double averageScore = attempts.stream()                      // средний балл
                .filter(a -> a.getScore() != null)                   // оставляем !=0
                .mapToInt(TestAttempt::getScore)
                .average()          // вычисляет среднее
                .orElse(0.0);

        List<Question> questions = questionRepository.findByTestId(testId);
        int maxPossibleScore = questions.stream()
                .mapToInt(Question::getPoints)
                .sum();

        return new TestResultsDto(test, attemptsCount, averageScore, maxPossibleScore);
    }
    public  Test getTest(Long testId){
            return testRepository.findById(testId)
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
    }

}