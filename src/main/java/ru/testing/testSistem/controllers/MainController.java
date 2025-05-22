package ru.testing.testSistem.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.testing.testSistem.services.TestTakingService;

@RequiredArgsConstructor

@Controller
public class MainController {
    private final TestTakingService testTakingService;

    @GetMapping({"/", "/userhome"})
    public String home() {
        return "userhome";
    }
    @ModelAttribute("homeUrl")
    public String getHomeUrl(@AuthenticationPrincipal UserDetails userDetails) {
        return testTakingService.getHomeUrl(userDetails);
    }
}