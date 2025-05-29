package ru.testing.testSistem.controllers;

//
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.testing.testSistem.DTO.AdminDashboardDto;

import ru.testing.testSistem.services.UserDashboardService;
import ru.testing.testSistem.services.UserManagementService;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserDashboardService userDashboardService;
    private final UserManagementService userManagementService;

    @GetMapping("/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AdminDashboardDto dashboard = userDashboardService.getAdminDashboard(userDetails.getUsername());

        model.addAttribute("tests", dashboard.getCreatedTests());
        model.addAttribute("completedTests", dashboard.getCompletedTests());
        model.addAttribute("username", dashboard.getUsername());

        return "admin/dashboard";
    }

    @PostMapping("/change-role")
    public String changeUserRole(
            @RequestParam String username,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        try {
            userManagementService.changeUserRole(username, role);
            redirectAttributes.addFlashAttribute("success",
                    "Роль пользователя " + username + " успешно изменена на " +
                            role.replace("ROLE_", ""));
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Некорректная роль");
        }

        return "redirect:/admin/dashboard";
    }
}