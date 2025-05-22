package ru.testing.testSistem.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testing.testSistem.services.RegistrationService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RegistrationService registrationService;

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        try {
            logger.debug("Обработка страницы входа: error={}, logout={}", error, logout);

            if (error != null) {
                logger.warn("Неудачная попытка входа");
                model.addAttribute("error", "Неверное имя пользователя или пароль");
            }

            if (logout != null) {
                logger.info("Успешный выход из системы");
                model.addAttribute("message", "Вы успешно вышли из системы");
            }

            return "auth/login";

        } catch (Exception e) {
            logger.error("Системная ошибка при отображении страницы входа", e);
            model.addAttribute("error", "Произошла системная ошибка");
            return "auth/login";
        }
    }
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            boolean verified = registrationService.verifyUser(token);
            if (verified) {
                model.addAttribute("message", "Email успешно подтвержден! Теперь вы можете войти.");
            }
            return "auth/login";
        } catch (Exception e) {
            logger.error("Ошибка подтверждения email", e);
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }
}
