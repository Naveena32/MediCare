package com.medicare.config;

import com.medicare.dto.RegisterDto;
import com.medicare.entity.Appointment;
import com.medicare.entity.MedicalRecord;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.MedicalRecordRepository;
import com.medicare.repository.PatientProfileRepository;
import com.medicare.repository.UserRepository;
import com.medicare.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService,
                                     UserRepository userRepository,
                                     PatientProfileRepository profileRepository,
                                     MedicalRecordRepository recordRepository,
                                     AppointmentRepository appointmentRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                // Create Demo Patient
                RegisterDto registerDto = new RegisterDto();
                registerDto.setUsername("johndoe");
                registerDto.setEmail("john@example.com");
                registerDto.setPassword("Password123!");
                registerDto.setConfirmPassword("Password123!");
                registerDto.setFullName("John Doe");

                User user = userService.registerNewUser(registerDto);
                user.setFirstName("John");
                user.setLastName("Doe");
                userRepository.save(user);

                PatientProfile profile = profileRepository.findByUser(user).orElse(new PatientProfile(user, "John Doe"));
                profile.setPhone("+1 (555) 0100");
                profile.setDateOfBirth(LocalDate.of(1990, 5, 15));
                profile.setGender("Male");
                profile.setBloodGroup("O+");
                profile.setEmergencyContact("Jane Doe - 555-0100");
                profile.setAddress("123 Health Ave, Suite 4");
                profile.setAllergies("Penicillin");
                profile.setCurrentMedications("Vitamin D 1000IU, Daily Multivitamin");
                profile.setExistingConditions("Mild Asthma");
                profile.setMedicalHistory("No major chronic illnesses. Regular annual physical checkups.");
                profileRepository.save(profile);

                // Sample Records
                MedicalRecord record1 = new MedicalRecord();
                record1.setUser(user);
                record1.setTitle("Comprehensive Metabolic Panel (CMP)");
                record1.setCategory("Lab Result");
                record1.setDoctorName("Sarah Jenkins");
                record1.setRecordDate(LocalDate.now().minusDays(15));
                record1.setNotes("Glucose and electrolyte levels are completely normal. Kidney and liver functions are optimal.");
                recordRepository.save(record1);

                MedicalRecord record2 = new MedicalRecord();
                record2.setUser(user);
                record2.setTitle("Chest X-Ray (PA & Lateral)");
                record2.setCategory("Scan / Imaging");
                record2.setDoctorName("Alan Grant");
                record2.setRecordDate(LocalDate.now().minusDays(45));
                record2.setNotes("Lungs are clear bilaterally. No pleural effusion or consolidation detected.");
                recordRepository.save(record2);

                // Sample Upcoming Appointment
                Appointment appt1 = new Appointment();
                appt1.setUser(user);
                appt1.setDoctorName("Gregory House");
                appt1.setDepartment("Cardiology");
                appt1.setAppointmentDate(LocalDate.now().plusDays(2));
                appt1.setAppointmentTime(LocalTime.of(10, 30));
                appt1.setReason("Annual Cardiovascular Follow-Up");
                appt1.setNotes("Review blood pressure logs and ECG results.");
                appt1.setStatus("Scheduled");
                appointmentRepository.save(appt1);
            }
        };
    }
}
