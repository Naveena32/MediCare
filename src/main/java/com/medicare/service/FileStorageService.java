package com.medicare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${medicare.upload.dir:./media/medical_records/}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        LocalDate now = LocalDate.now();
        String subPath = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        Path targetDir = this.rootLocation.resolve(subPath);

        try {
            Files.createDirectories(targetDir);
            String uniqueName = UUID.randomUUID().toString() + extension;
            Path destinationFile = targetDir.resolve(uniqueName).normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/media/medical_records/" + subPath + "/" + uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            String pathPart = relativePath.replaceFirst("^/media/medical_records/", "");
            Path file = this.rootLocation.resolve(pathPart).normalize().toAbsolutePath();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}
