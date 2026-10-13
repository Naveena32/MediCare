package com.medicare.controller;

import com.medicare.dto.ContactMessageDto;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
import com.medicare.service.ContactMessageService;
import com.medicare.service.PatientProfileService;
import com.medicare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {

    private final ContactMessageService contactMessageService;
    private final UserService userService;
    private final PatientProfileService profileService;

    public ContactController(ContactMessageService contactMessageService,
                             UserService userService,
                             PatientProfileService profileService) {
        this.contactMessageService = contactMessageService;
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping
    public String contactForm(Model model) {
        User user = userService.getCurrentUser();
        ContactMessageDto dto = new ContactMessageDto();
        if (user != null) {
            PatientProfile profile = profileService.getOrCreateProfile(user);
            dto.setName(profile.getFullName() != null && !profile.getFullName().isBlank() ? profile.getFullName() : user.getUsername());
            dto.setEmail(user.getEmail());
        }

        model.addAttribute("contactDto", dto);
        return "contact";
    }

    @PostMapping
    public String handleContactSubmit(@Valid @ModelAttribute("contactDto") ContactMessageDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Please ensure all required fields are filled out correctly.");
            return "contact";
        }

        contactMessageService.saveMessage(dto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you! Your message has been sent. Our support team will reply to your email shortly.");
        return "redirect:/contact";
    }
}
