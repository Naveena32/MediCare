package com.medicare.controller;

import com.medicare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (userService.getCurrentUser() != null) {
            return "redirect:/dashboard";
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been successfully logged out.");
        }
        return "home";
    }
}
