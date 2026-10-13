package com.medicare.controller;

import com.medicare.dto.AppointmentDto;
import com.medicare.entity.Appointment;
import com.medicare.entity.User;
import com.medicare.service.AppointmentService;
import com.medicare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public static final List<String> DEPARTMENTS = Arrays.asList(
            "General Medicine", "Cardiology", "Dermatology", "Pediatrics",
            "Orthopedics", "Neurology", "Gynecology", "Dentistry",
            "Ophthalmology", "Other"
    );

    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping
    public String appointmentsList(@RequestParam(value = "status", required = false, defaultValue = "all") String status,
                                   Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<Appointment> appointments = appointmentService.getAppointments(user, status);

        model.addAttribute("appointments", appointments);
        model.addAttribute("statusFilter", status);
        model.addAttribute("today", LocalDate.now());

        return "appointments/list";
    }

    @GetMapping("/book")
    public String bookAppointmentForm(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("appointmentDto", new AppointmentDto());
        model.addAttribute("departments", DEPARTMENTS);
        return "appointments/book";
    }

    @PostMapping("/book")
    public String handleBookAppointment(@Valid @ModelAttribute("appointmentDto") AppointmentDto appointmentDto,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", DEPARTMENTS);
            model.addAttribute("errorMessage", "Please check your appointment details.");
            return "appointments/book";
        }

        Appointment appt = appointmentService.bookAppointment(user, appointmentDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Appointment with Dr. " + appt.getDoctorName() + " booked for " + appt.getAppointmentDate() + "!");
        return "redirect:/appointments";
    }

    @GetMapping("/{id}/cancel")
    public String confirmCancelAppointment(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Appointment appt = appointmentService.getAppointmentByIdAndUser(id, user).orElse(null);
        if (appt == null) {
            return "redirect:/appointments";
        }

        model.addAttribute("appointment", appt);
        return "appointments/confirm-cancel";
    }

    @PostMapping("/{id}/cancel")
    public String handleCancelAppointment(@PathVariable("id") Long id,
                                          RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Appointment appt = appointmentService.getAppointmentByIdAndUser(id, user).orElse(null);
        if (appt != null) {
            appointmentService.cancelAppointment(appt);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Appointment with Dr. " + appt.getDoctorName() + " has been cancelled.");
        }

        return "redirect:/appointments";
    }
}
