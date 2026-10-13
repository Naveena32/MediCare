package com.medicare.service;

import com.medicare.dto.AppointmentDto;
import com.medicare.entity.Appointment;
import com.medicare.entity.User;
import com.medicare.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAppointments(User user, String statusFilter) {
        LocalDate today = LocalDate.now();
        if ("upcoming".equalsIgnoreCase(statusFilter)) {
            return appointmentRepository.findUpcomingAppointments(user, today);
        } else if ("past".equalsIgnoreCase(statusFilter)) {
            return appointmentRepository.findPastAppointments(user, today);
        } else if ("cancelled".equalsIgnoreCase(statusFilter)) {
            return appointmentRepository.findByUserAndStatusOrderByAppointmentDateDescAppointmentTimeDesc(user, "Cancelled");
        } else {
            return appointmentRepository.findByUserOrderByAppointmentDateAscAppointmentTimeAsc(user);
        }
    }

    public List<Appointment> getUpcomingAppointments(User user, int limit) {
        List<Appointment> upcoming = appointmentRepository.findUpcomingAppointments(user, LocalDate.now());
        return upcoming.size() > limit ? upcoming.subList(0, limit) : upcoming;
    }

    public long countUpcomingAppointments(User user) {
        return appointmentRepository.countUpcomingAppointments(user, LocalDate.now());
    }

    public Optional<Appointment> getAppointmentByIdAndUser(Long id, User user) {
        return appointmentRepository.findByIdAndUser(id, user);
    }

    @Transactional
    public Appointment bookAppointment(User user, AppointmentDto dto) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctorName(dto.getDoctorName().trim());
        appointment.setDepartment(dto.getDepartment());
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setReason(dto.getReason().trim());
        appointment.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : "");
        appointment.setStatus("Scheduled");

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Appointment appointment) {
        appointment.setStatus("Cancelled");
        return appointmentRepository.save(appointment);
    }
}
