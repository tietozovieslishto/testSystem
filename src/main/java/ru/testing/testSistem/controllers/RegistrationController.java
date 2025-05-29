package ru.testing.testSistem.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.testing.testSistem.DTO.RegistrationDto;
import ru.testing.testSistem.services.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

   @PostMapping("/register")
   public String registerUser(
           @Valid @ModelAttribute("registrationDto") RegistrationDto registrationDto,
           BindingResult bindingResult,
           RedirectAttributes redirectAttributes) {

       if (bindingResult.hasErrors()) {
           logger.warn("Ошибка валидации для пользователя: {}", registrationDto.getUsername());
           return "auth/register";
       }
       logger.info("Регистрация пользователя: {}", registrationDto.getUsername());

       try {
           registrationService.registerUser(registrationDto);
           redirectAttributes.addFlashAttribute("success",
                   "Регистрация успешна! Пожалуйста, проверьте ваш email для подтверждения.");
           logger.info("Пользователь {} успешно зарегистрирован", registrationDto.getUsername());

           return "redirect:/auth/login";
       } catch (Exception e) {
           logger.error("Ошибка регистрации пользователя: {}", registrationDto.getUsername(), e);
           bindingResult.reject("registration.error", e.getMessage()); // в help
           return "auth/register";
       }
   }
}