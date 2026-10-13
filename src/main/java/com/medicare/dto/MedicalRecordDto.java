package com.medicare.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class MedicalRecordDto {

    private Long id;

    @NotBlank(message = "Record title is required")
    private String title;

    @NotBlank(message = "Category is required")
    private String category = "Lab Result";

    private String doctorName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDate;

    private String notes;

    private MultipartFile file;

    private String existingFileName;
    private String existingFilePath;

    public MedicalRecordDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getExistingFileName() {
        return existingFileName;
    }

    public void setExistingFileName(String existingFileName) {
        this.existingFileName = existingFileName;
    }

    public String getExistingFilePath() {
        return existingFilePath;
    }

    public void setExistingFilePath(String existingFilePath) {
        this.existingFilePath = existingFilePath;
    }
}
