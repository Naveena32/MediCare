package com.medicare;

import com.medicare.entity.Appointment;
import com.medicare.entity.ContactMessage;
import com.medicare.entity.MedicalRecord;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
import com.medicare.repository.AppointmentRepository;
import com.medicare.repository.ContactMessageRepository;
import com.medicare.repository.MedicalRecordRepository;
import com.medicare.repository.PatientProfileRepository;
import com.medicare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MediCareApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileRepository profileRepository;

    @Autowired
    private MedicalRecordRepository recordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ContactMessageRepository contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private PatientProfile testProfile;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        recordRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        contactRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole("ROLE_USER");
        testUser = userRepository.save(testUser);

        testProfile = new PatientProfile(testUser, "John Doe");
        testProfile.setBloodGroup("O+");
        testProfile.setEmergencyContact("Jane Doe - 555-0100");
        testProfile.setAllergies("Penicillin");
        testProfile = profileRepository.save(testProfile);
    }

    // 1. Authentication Tests
    @Test
    void testRegisterCreatesUserAndProfileAndLogsIn() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newpatient")
                .param("email", "newpatient@example.com")
                .param("password", "StrongPass123")
                .param("confirmPassword", "StrongPass123")
                .param("fullName", "New Patient"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        assertTrue(userRepository.existsByUsernameIgnoreCase("newpatient"));
        User newUser = userRepository.findByUsernameIgnoreCase("newpatient").orElseThrow();
        PatientProfile profile = profileRepository.findByUser(newUser).orElseThrow();
        assertEquals("New Patient", profile.getFullName());
    }

    @Test
    void testLoginWithEmailAndLogout() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", "john@example.com")
                .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?logout=true"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", "john@example.com")
                .param("password", "WrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 2. Medical Records CRUD Tests
    @Test
    @WithMockUser(username = "johndoe")
    void testUploadMedicalRecordWithFile() throws Exception {
        MockMultipartFile dummyFile = new MockMultipartFile(
                "file",
                "lab_report.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        mockMvc.perform(multipart("/records/upload")
                .file(dummyFile)
                .with(csrf())
                .param("title", "Complete Blood Count (CBC)")
                .param("category", "Lab Result")
                .param("doctorName", "Sarah Jenkins")
                .param("recordDate", "2026-09-10")
                .param("notes", "Normal WBC and RBC levels"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/records"));

        List<MedicalRecord> records = recordRepository.findByUserOrderByUploadedAtDesc(testUser);
        assertEquals(1, records.size());
        MedicalRecord record = records.get(0);
        assertEquals("Complete Blood Count (CBC)", record.getTitle());
        assertEquals("Sarah Jenkins", record.getDoctorName());
        assertTrue(record.hasFile());
    }

    @Test
    @WithMockUser(username = "johndoe")
    void testViewRecordsSearchAndFilter() throws Exception {
        MedicalRecord r1 = new MedicalRecord();
        r1.setUser(testUser);
        r1.setTitle("MRI Brain Scan");
        r1.setCategory("Scan / Imaging");
        r1.setDoctorName("Alan Grant");
        r1.setNotes("Clear scan");
        recordRepository.save(r1);

        MedicalRecord r2 = new MedicalRecord();
        r2.setUser(testUser);
        r2.setTitle("Lipid Panel");
        r2.setCategory("Lab Result");
        r2.setDoctorName("Sarah Jenkins");
        r2.setNotes("Low cholesterol");
        recordRepository.save(r2);

        // Test search query
        mockMvc.perform(get("/records").param("q", "Brain"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalCount", 1));

        // Test category filter
        mockMvc.perform(get("/records").param("category", "Lab Result"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalCount", 1));
    }

    @Test
    @WithMockUser(username = "johndoe")
    void testEditAndDeleteMedicalRecord() throws Exception {
        MedicalRecord record = new MedicalRecord();
        record.setUser(testUser);
        record.setTitle("Initial Record");
        record.setCategory("Prescription");
        record.setNotes("Old notes");
        record = recordRepository.save(record);

        // Edit record
        mockMvc.perform(post("/records/" + record.getId() + "/edit")
                .with(csrf())
                .param("title", "Updated Prescription")
                .param("category", "Prescription")
                .param("notes", "Take with water twice daily"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/records/" + record.getId()));

        MedicalRecord updated = recordRepository.findById(record.getId()).orElseThrow();
        assertEquals("Updated Prescription", updated.getTitle());

        // Delete record
        mockMvc.perform(post("/records/" + record.getId() + "/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/records"));

        assertFalse(recordRepository.findById(record.getId()).isPresent());
    }

    // 3. Appointments Booking & Management Tests
    @Test
    @WithMockUser(username = "johndoe")
    void testBookAndCancelAppointment() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Book appointment
        mockMvc.perform(post("/appointments/book")
                .with(csrf())
                .param("doctorName", "Gregory House")
                .param("department", "Cardiology")
                .param("appointmentDate", tomorrow.toString())
                .param("appointmentTime", "10:00")
                .param("reason", "Chest consultation")
                .param("notes", "First time visit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));

        List<Appointment> appts = appointmentRepository.findByUserOrderByAppointmentDateAscAppointmentTimeAsc(testUser);
        assertEquals(1, appts.size());
        Appointment appt = appts.get(0);
        assertEquals("Gregory House", appt.getDoctorName());
        assertEquals("Scheduled", appt.getStatus());

        // Cancel appointment
        mockMvc.perform(post("/appointments/" + appt.getId() + "/cancel")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));

        Appointment cancelled = appointmentRepository.findById(appt.getId()).orElseThrow();
        assertEquals("Cancelled", cancelled.getStatus());
    }

    // 4. Patient Profile Editing Tests
    @Test
    @WithMockUser(username = "johndoe")
    void testEditPatientProfile() throws Exception {
        mockMvc.perform(post("/profile/edit")
                .with(csrf())
                .param("firstName", "Johnny")
                .param("lastName", "Doe")
                .param("fullName", "Johnny Doe")
                .param("email", "johnny@example.com")
                .param("phone", "+1 555-9876")
                .param("gender", "Male")
                .param("bloodGroup", "AB+")
                .param("emergencyContact", "Sarah Doe - 555-4321")
                .param("address", "456 Medical Park, CA")
                .param("allergies", "Latex, Penicillin")
                .param("currentMedications", "Vitamin D 1000IU")
                .param("existingConditions", "Mild Asthma")
                .param("pastSurgeries", "None")
                .param("medicalHistory", "No major history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        PatientProfile updatedProfile = profileRepository.findByUser(testUser).orElseThrow();
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();

        assertEquals("Johnny Doe", updatedProfile.getFullName());
        assertEquals("AB+", updatedProfile.getBloodGroup());
        assertEquals("+1 555-9876", updatedProfile.getPhone());
        assertEquals("johnny@example.com", updatedUser.getEmail());
        assertEquals("Johnny", updatedUser.getFirstName());
    }

    // 5. Contact Inquiries Tests
    @Test
    void testSubmitContactMessage() throws Exception {
        mockMvc.perform(post("/contact")
                .with(csrf())
                .param("name", "Alice Smith")
                .param("email", "alice@example.com")
                .param("subject", "Insurance Inquiry")
                .param("message", "Do you accept Blue Cross Blue Shield insurance plans?"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact"));

        List<ContactMessage> messages = contactRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals("Alice Smith", messages.get(0).getName());
        assertEquals("Insurance Inquiry", messages.get(0).getSubject());
    }

    // 6. Unauthenticated Access Protection
    @Test
    void testProtectedRoutesRequireLogin() throws Exception {
        String[] protectedUrls = {
                "/dashboard",
                "/records",
                "/records/upload",
                "/appointments",
                "/appointments/book",
                "/profile",
                "/profile/edit",
                "/password-change"
        };

        for (String url : protectedUrls) {
            mockMvc.perform(get(url))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }
}
