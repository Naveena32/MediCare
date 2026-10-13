package com.medicare.service;

import com.medicare.dto.PatientProfileDto;
import com.medicare.entity.PatientProfile;
import com.medicare.entity.User;
import com.medicare.repository.PatientProfileRepository;
import com.medicare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    public PatientProfileService(PatientProfileRepository patientProfileRepository,
                                 UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    public PatientProfile getOrCreateProfile(User user) {
        return patientProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    PatientProfile profile = new PatientProfile();
                    profile.setUser(user);
                    profile.setFullName(user.getFullName());
                    return patientProfileRepository.save(profile);
                });
    }

    public int calculateProfileCompletion(PatientProfile profile, User user) {
        int totalFields = 9;
        int completed = 0;

        if (profile.getFullName() != null && !profile.getFullName().isBlank()) completed++;
        if (user.getEmail() != null && !user.getEmail().isBlank()) completed++;
        if (profile.getPhone() != null && !profile.getPhone().isBlank()) completed++;
        if (profile.getDateOfBirth() != null) completed++;
        if (profile.getGender() != null && !profile.getGender().isBlank()) completed++;
        if (profile.getBloodGroup() != null && !profile.getBloodGroup().isBlank()) completed++;
        if (profile.getEmergencyContact() != null && !profile.getEmergencyContact().isBlank()) completed++;
        if (profile.getAddress() != null && !profile.getAddress().isBlank()) completed++;
        if ((profile.getMedicalHistory() != null && !profile.getMedicalHistory().isBlank()) ||
            (profile.getAllergies() != null && !profile.getAllergies().isBlank()) ||
            (profile.getCurrentMedications() != null && !profile.getCurrentMedications().isBlank())) {
            completed++;
        }

        return (int) Math.round(((double) completed / totalFields) * 100);
    }

    @Transactional
    public PatientProfile updateProfile(User user, PatientProfileDto dto) {
        PatientProfile profile = getOrCreateProfile(user);

        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setBloodGroup(dto.getBloodGroup());
        profile.setEmergencyContact(dto.getEmergencyContact());
        profile.setAddress(dto.getAddress());
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile.setAllergies(dto.getAllergies());
        profile.setCurrentMedications(dto.getCurrentMedications());
        profile.setPastSurgeries(dto.getPastSurgeries());
        profile.setExistingConditions(dto.getExistingConditions());

        // Update User first_name, last_name, email
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail().trim().toLowerCase());
        }
        userRepository.save(user);

        return patientProfileRepository.save(profile);
    }
}
