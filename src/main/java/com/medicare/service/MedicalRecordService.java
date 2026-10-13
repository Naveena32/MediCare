package com.medicare.service;

import com.medicare.dto.MedicalRecordDto;
import com.medicare.entity.MedicalRecord;
import com.medicare.entity.User;
import com.medicare.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final FileStorageService fileStorageService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                FileStorageService fileStorageService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<MedicalRecord> getRecords(User user, String query, String category) {
        return medicalRecordRepository.searchRecords(user, query != null ? query.trim() : null, category != null ? category.trim() : null);
    }

    public List<MedicalRecord> getRecentRecords(User user, int limit) {
        List<MedicalRecord> records = medicalRecordRepository.findByUserOrderByUploadedAtDesc(user);
        return records.size() > limit ? records.subList(0, limit) : records;
    }

    public long countRecords(User user) {
        return medicalRecordRepository.countByUser(user);
    }

    public Optional<MedicalRecord> getRecordByIdAndUser(Long id, User user) {
        return medicalRecordRepository.findByIdAndUser(id, user);
    }

    @Transactional
    public MedicalRecord createRecord(User user, MedicalRecordDto dto) {
        MedicalRecord record = new MedicalRecord();
        record.setUser(user);
        record.setTitle(dto.getTitle().trim());
        record.setCategory(dto.getCategory());
        record.setDoctorName(dto.getDoctorName() != null ? dto.getDoctorName().trim() : "");
        record.setRecordDate(dto.getRecordDate());
        record.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : "");

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String filePath = fileStorageService.store(dto.getFile());
            record.setFilePath(filePath);
            record.setFileName(dto.getFile().getOriginalFilename());
        }

        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord updateRecord(MedicalRecord record, MedicalRecordDto dto) {
        record.setTitle(dto.getTitle().trim());
        record.setCategory(dto.getCategory());
        record.setDoctorName(dto.getDoctorName() != null ? dto.getDoctorName().trim() : "");
        record.setRecordDate(dto.getRecordDate());
        record.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : "");

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            if (record.getFilePath() != null) {
                fileStorageService.delete(record.getFilePath());
            }
            String filePath = fileStorageService.store(dto.getFile());
            record.setFilePath(filePath);
            record.setFileName(dto.getFile().getOriginalFilename());
        }

        return medicalRecordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(MedicalRecord record) {
        if (record.getFilePath() != null) {
            fileStorageService.delete(record.getFilePath());
        }
        medicalRecordRepository.delete(record);
    }
}
