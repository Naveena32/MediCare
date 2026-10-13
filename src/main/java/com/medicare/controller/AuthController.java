package com.medicare.controller;

import com.medicare.dto.PasswordChangeDto;
import com.medicare.dto.RegisterDto;
import com.medicare.entity.User;
import com.medicare.service.CustomUserDetailsService;
import com.medicare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (userService.getCurrentUser() != null) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid credentials. Please check your username/email and password.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been successfully logged out.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (userService.getCurrentUser() != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (userService.getCurrentUser() != null) {
            return "redirect:/dashboard";
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "register";
        }

        if (userService.existsByUsername(registerDto.getUsername())) {
            model.addAttribute("errorMessage", "A user with that username already exists.");
            return "register";
        }

        if (userService.existsByEmail(registerDto.getEmail())) {
            model.addAttribute("errorMessage", "A user with that email already exists.");
            return "register";
        }

        User user = userService.registerNewUser(registerDto);

        // Programmatically authenticate user into Spring Security session
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        redirectAttributes.addFlashAttribute("successMessage",
                "Account created successfully! Welcome to MediCare, " + user.getUsername() + ".");
        return "redirect:/dashboard";
    }

    @GetMapping("/password-change")
    public String passwordChangePage(Model model) {
        model.addAttribute("passwordDto", new PasswordChangeDto());
        return "password_change";
    }

    @PostMapping("/password-change")
    public String handlePasswordChange(@Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "password_change";
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "New passwords do not match.");
            return "password_change";
        }

        boolean updated = userService.changePassword(currentUser, passwordDto.getOldPassword(), passwordDto.getNewPassword());
        if (!updated) {
            model.addAttribute("errorMessage", "Current password is incorrect.");
            return "password_change";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your password was successfully updated!");
        return "redirect:/profile";
    }
}
