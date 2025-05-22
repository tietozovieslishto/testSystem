package ru.testing.testSistem.controllers;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.testing.testSistem.DTO.*;
import ru.testing.testSistem.exception.ResourceNotFoundException;
import ru.testing.testSistem.models.*;
import ru.testing.testSistem.repo.*;
import ru.testing.testSistem.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;


@Controller
@RequestMapping("/tester")
@RequiredArgsConstructor
public class TesterController {
    private final TesterService testerService;
    private final TestPermissionService testPermissionService;
    private final EmailService emailService;

    @GetMapping("/dashboard")
    public String testerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        TesterDashboardDto dashboard = testerService.getTesterDashboard(userDetails.getUsername());
        model.addAttribute("tests", dashboard.getCreatedTests());
        model.addAttribute("completedTests", dashboard.getCompletedTests());
        model.addAttribute("username", dashboard.getUsername());
        return "tester/dashboard";
    }

    @GetMapping("/tests/new")
    public String showCreateTestForm(Model model) {
        model.addAttribute("testForm", new TestForm());
        return "tester/create-test";
    }

    @PostMapping("/tests")
    public String createTest(@Valid @ModelAttribute TestForm testForm,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "tester/create-test";
        }

        Test test = testerService.createTest(testForm, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Тест успешно создан!");
        return "redirect:/tester/tests/" + test.getId() + "/questions";
    }

    @GetMapping("/tests/{testId}/questions")
    public String manageQuestions(@PathVariable Long testId,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        TestQuestionsDto questionsData = testerService.getTestQuestionsData(testId, userDetails.getUsername());
        model.addAttribute("test", questionsData.getTest());
        model.addAttribute("questions", questionsData.getQuestions());
        model.addAttribute("questionForm", new QuestionForm());
        model.addAttribute("answerForm", new AnswerForm());
        return "tester/manage-questions";
    }

    @PostMapping("/tests/{testId}/questions")
    public String addQuestion(@PathVariable Long testId,
                              @Valid @ModelAttribute QuestionForm questionForm,
                              BindingResult result,
                              @RequestParam(required = false) MultipartFile image,
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.questionForm", result);
            redirectAttributes.addFlashAttribute("questionForm", questionForm);
            return "redirect:/tester/tests/" + testId + "/questions";
        }

        testerService.addQuestion(testId, questionForm, image, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Вопрос добавлен!");
        return "redirect:/tester/tests/" + testId + "/questions";
    }

    @PostMapping("/tests/{testId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long testId,
                                 @PathVariable Long questionId,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        testerService.deleteQuestion(testId, questionId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Вопрос удален!");
        return "redirect:/tester/tests/" + testId + "/questions";
    }

    @PostMapping("/tests/{testId}/questions/{questionId}/answers")
    public String addAnswer(@PathVariable Long testId,
                            @PathVariable Long questionId,
                            @Valid @ModelAttribute("answerForm") AnswerForm answerForm,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.answerForm", result);
            redirectAttributes.addFlashAttribute("answerForm", answerForm);
            return "redirect:/tester/tests/" + testId + "/questions";
        }

        testerService.addAnswer(testId, questionId, answerForm, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Ответ добавлен!");
        return "redirect:/tester/tests/" + testId + "/questions";
    }

    @PostMapping("/tests/{testId}/questions/{questionId}/answers/{answerId}/delete")
    public String deleteAnswer(@PathVariable Long testId,
                               @PathVariable Long questionId,
                               @PathVariable Long answerId,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        testerService.deleteAnswer(testId, answerId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Ответ удален!");
        return "redirect:/tester/tests/" + testId + "/questions";
    }

    @GetMapping("/tests/{testId}/permissions")
    public String managePermissions(@PathVariable Long testId,
                                    @RequestParam(required = false) String search,
                                    Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        TestPermissionsDto permissions = testerService.getTestPermissions(testId, search, userDetails.getUsername());
        model.addAttribute("test", permissions.getTest());
        model.addAttribute("usersToAdd", permissions.getUsersToAdd());
        model.addAttribute("allowedUsers", permissions.getAllowedUsers());
        model.addAttribute("searchQuery", search);
        return "tester/manage-permissions";
    }

    @PostMapping("/tests/{testId}/permissions/grant")
    public String grantAccess(@PathVariable Long testId,
                              @RequestParam Long userId,
                              RedirectAttributes redirectAttributes) {
        testPermissionService.grantAccess(testId, userId);
        redirectAttributes.addFlashAttribute("success", "Доступ предоставлен!");
        return "redirect:/tester/tests/" + testId + "/permissions";
    }

    @PostMapping("/tests/{testId}/permissions/revoke")
    public String revokeAccess(@PathVariable Long testId,
                               @RequestParam Long userId,
                               RedirectAttributes redirectAttributes) {
        testPermissionService.revokeAccess(testId, userId);
        redirectAttributes.addFlashAttribute("success", "Доступ отозван!");
        return "redirect:/tester/tests/" + testId + "/permissions";
    }

    @PostMapping("/tests/{testId}/toggle-privacy")
    public String togglePrivacy(@PathVariable Long testId,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        testerService.toggleTestPrivacy(testId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Настройки приватности теста обновлены");
        return "redirect:/tester/tests/" + testId + "/permissions";
    }

    @GetMapping("/tests/{id}/results")
    public String showTestResults(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        TestResultsDto results = testerService.getTestResults(id, userDetails.getUsername());
        model.addAttribute("test", results.getTest());
        model.addAttribute("attemptsCount", results.getAttemptsCount());
        model.addAttribute("averageScore", results.getAverageScore());
        model.addAttribute("maxPossibleScore", results.getMaxPossibleScore());
        return "tester/test-results";
    }

    @PostMapping("/tests/{testId}/toggle-status")
    public String toggleTestStatus(@PathVariable Long testId,
                                   RedirectAttributes redirectAttributes,
                                   @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        testerService.toggleTestStatus(testId, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("success", "Статус теста изменен");
        return "redirect:/tester/dashboard";
    }

    @GetMapping("/tests/{testId}/invite")
    public String showInviteForm(@PathVariable Long testId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) throws AccessDeniedException {
        Test test = testerService.getTestForInvitation(testId, userDetails.getUsername());
        model.addAttribute("test", test);
        return "tester/send-invite";
    }
    private final UserRepository userRepository;
    private final TestRepository testRepository;

    @PostMapping("/tests/{testId}/send-invite")
    public String sendTestInvitation(@PathVariable Long testId,
                                     @RequestParam String email,
                                     @RequestParam(required = false) String message,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        User sender = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        try {
            emailService.sendTestInvitation(sender, email, test, message);
            redirectAttributes.addFlashAttribute("success", "Приглашение успешно отправлено на " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось отправить приглашение: " + e.getMessage());
        }

        return "redirect:/tester/tests/" + testId + "/invite";
    }

    @ModelAttribute("homeUrl")
    public String getHomeUrl(@AuthenticationPrincipal UserDetails userDetails) {
        return testerService.getHomeUrl(userDetails);
    }
}