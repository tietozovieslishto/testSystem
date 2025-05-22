package ru.testing.testSistem.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.testing.testSistem.DTO.TestQuestionDto;
import ru.testing.testSistem.DTO.TestResultDto;
import ru.testing.testSistem.models.Test;
import ru.testing.testSistem.models.TestAttempt;
import ru.testing.testSistem.services.TestResultService;
import ru.testing.testSistem.services.TestTakingService;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestTakingController {
    private final TestTakingService testTakingService;
    private final TestResultService testResultService;
    private static final Logger logger = LoggerFactory.getLogger(TestTakingController.class);

    @GetMapping("/available")
    public String showAvailableTests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            Model model) {

        List<Test> availableTests = testTakingService.getAvailableTests(userDetails.getUsername(), search);
        model.addAttribute("tests", availableTests);
        model.addAttribute("searchTerm", search != null ? search : "");
        return "tests/available-tests";
    }

    @GetMapping("/{testId}/start")
    public String startTest(
            @PathVariable Long testId,
            @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {

        TestAttempt attempt = testTakingService.startTest(testId, userDetails.getUsername());
        return "redirect:/tests/" + testId + "/attempt/" + attempt.getId() + "/question/1";
    }

    @GetMapping("/{testId}/attempt/{attemptId}/question/{questionNumber}")
    public String showQuestion(
            @PathVariable Long testId,
            @PathVariable Long attemptId,
            @PathVariable int questionNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        TestQuestionDto questionData = testTakingService.getQuestionData(
                testId, attemptId, questionNumber, userDetails.getUsername());

        if (questionData.isTimeExpired()) {
            return "redirect:/tests/" + testId + "/attempt/" + attemptId + "/results?timeout=true";
        }

        model.addAttribute("test", questionData.getTest());
        model.addAttribute("attempt", questionData.getAttempt());
        model.addAttribute("question", questionData.getQuestion());
        model.addAttribute("questionNumber", questionNumber);
        model.addAttribute("totalQuestions", questionData.getTotalQuestions());

        return "tests/test-question";
    }

    @PostMapping("/{testId}/attempt/{attemptId}/question/{questionNumber}")
    public String saveAnswer(
            @PathVariable Long testId,
            @PathVariable Long attemptId,
            @PathVariable int questionNumber,
            @RequestParam(required = false) List<Long> answerIds,
            @RequestParam(required = false) String textAnswer,
            @AuthenticationPrincipal UserDetails userDetails) {

        int nextQuestion = testTakingService.saveAnswerAndGetNextQuestion(
                attemptId, testId, questionNumber, answerIds, textAnswer, userDetails.getUsername());

        if (nextQuestion <= 0) {
            return "redirect:/tests/" + testId + "/attempt/" + attemptId + "/results";
        }
        return "redirect:/tests/" + testId + "/attempt/" + attemptId + "/question/" + nextQuestion;
    }

    @GetMapping("/{testId}/attempt/{attemptId}/results")
    public String showResults(
            @PathVariable Long testId,
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        TestResultDto result = testResultService.getTestResult(testId, attemptId, userDetails.getUsername());
        model.addAttribute("attempt", result.getAttempt());
        model.addAttribute("userAnswers", result.getUserAnswers());
        model.addAttribute("questions", result.getQuestions());
        model.addAttribute("correctTextAnswers", result.getCorrectTextAnswers());
        model.addAttribute("totalQuestions", result.getTotalQuestions());
        model.addAttribute("questionCorrectness", result.getQuestionCorrectness());

        return "tests/test-results";
    }

    @ModelAttribute("homeUrl")
    public String getHomeUrl(@AuthenticationPrincipal UserDetails userDetails) {
        return testTakingService.getHomeUrl(userDetails);
    }
}