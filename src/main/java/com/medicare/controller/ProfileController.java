package com.medicare.controller;

import com.medicare.dto.PatientProfileDto;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PatientProfileService profileService;

    public static final List<String> GENDER_CHOICES = Arrays.asList(
            "Male", "Female", "Other", "Prefer not to say"
    );

    public static final List<String> BLOOD_GROUPS = Arrays.asList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    public ProfileController(UserService userService, PatientProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping
    public String profileView(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        PatientProfile profile = profileService.getOrCreateProfile(user);
        int completion = profileService.calculateProfileCompletion(profile, user);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("completion", completion);

        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        PatientProfile profile = profileService.getOrCreateProfile(user);

        PatientProfileDto dto = new PatientProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(profile.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setGender(profile.getGender());
        dto.setBloodGroup(profile.getBloodGroup());
        dto.setEmergencyContact(profile.getEmergencyContact());
        dto.setAddress(profile.getAddress());
        dto.setMedicalHistory(profile.getMedicalHistory());
        dto.setAllergies(profile.getAllergies());
        dto.setCurrentMedications(profile.getCurrentMedications());
        dto.setPastSurgeries(profile.getPastSurgeries());
        dto.setExistingConditions(profile.getExistingConditions());

        model.addAttribute("profileDto", dto);
        model.addAttribute("profile", profile);
        model.addAttribute("genderChoices", GENDER_CHOICES);
        model.addAttribute("bloodGroups", BLOOD_GROUPS);

        return "profile/edit";
    }

    @PostMapping("/edit")
    public String handleEditProfile(@Valid @ModelAttribute("profileDto") PatientProfileDto dto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        PatientProfile profile = profileService.getOrCreateProfile(user);

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            model.addAttribute("genderChoices", GENDER_CHOICES);
            model.addAttribute("bloodGroups", BLOOD_GROUPS);
            model.addAttribute("errorMessage", "Please correct the errors in the profile form.");
            return "profile/edit";
        }

        // Check if email already belongs to another user
        if (!dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            Optional<User> existingWithEmail = userService.findByEmail(dto.getEmail());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(user.getId())) {
                model.addAttribute("profile", profile);
                model.addAttribute("genderChoices", GENDER_CHOICES);
                model.addAttribute("bloodGroups", BLOOD_GROUPS);
                model.addAttribute("errorMessage", "That email address is already in use by another account.");
                return "profile/edit";
            }
        }

        profileService.updateProfile(user, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Your health profile has been updated successfully!");
        return "redirect:/profile";
    }
}
