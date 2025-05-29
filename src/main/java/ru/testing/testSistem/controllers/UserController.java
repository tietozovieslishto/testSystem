package ru.testing.testSistem.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.testing.testSistem.DTO.UserDashboardData;
import lombok.RequiredArgsConstructor;
import ru.testing.testSistem.services.UserDashboardService;
//
@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserDashboardService userDashboardService;

    @GetMapping("/dashboard")
    public String userDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDashboardData dashboardData =
                userDashboardService.getUserDashboardData(userDetails.getUsername());

        model.addAttribute("completedTests", dashboardData.getCompletedTests());
        model.addAttribute("username", dashboardData.getUsername());

        return "user/dashboard";
    }
}