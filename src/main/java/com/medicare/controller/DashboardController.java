package com.medicare.controller;

import com.medicare.entity.Appointment;
import com.medicare.entity.MedicalRecord;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
import com.medicare.service.AppointmentService;
import com.medicare.service.MedicalRecordService;
import com.medicare.service.PatientProfileService;
import com.medicare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final PatientProfileService profileService;
    private final MedicalRecordService recordService;
    private final AppointmentService appointmentService;

    public DashboardController(UserService userService,
                               PatientProfileService profileService,
                               MedicalRecordService recordService,
                               AppointmentService appointmentService) {
        this.userService = userService;
        this.profileService = profileService;
        this.recordService = recordService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        PatientProfile profile = profileService.getOrCreateProfile(user);
        List<MedicalRecord> recentRecords = recordService.getRecentRecords(user, 5);
        long totalRecords = recordService.countRecords(user);

        List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointments(user, 3);
        long totalUpcoming = appointmentService.countUpcomingAppointments(user);

        int profileCompletion = profileService.calculateProfileCompletion(profile, user);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("recentRecords", recentRecords);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("totalUpcoming", totalUpcoming);
        model.addAttribute("profileCompletion", profileCompletion);

        return "dashboard";
    }
}
