package com.medicare.repository;

import com.medicare.entity.Appointment;
import com.medicare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserOrderByAppointmentDateAscAppointmentTimeAsc(User user);
    Optional<Appointment> findByIdAndUser(Long id, User user);

    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate >= :today AND a.status = 'Scheduled' ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user = :user AND a.appointmentDate >= :today AND a.status = 'Scheduled'")
    long countUpcomingAppointments(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND (a.appointmentDate < :today OR a.status = 'Completed') ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findPastAppointments(@Param("user") User user, @Param("today") LocalDate today);

    List<Appointment> findByUserAndStatusOrderByAppointmentDateDescAppointmentTimeDesc(User user, String status);
}
