package com.medicare.repository;

import com.medicare.entity.MedicalRecord;
import com.medicare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByUserOrderByUploadedAtDesc(User user);
    long countByUser(User user);
    Optional<MedicalRecord> findByIdAndUser(Long id, User user);

    @Query("SELECT r FROM MedicalRecord r WHERE r.user = :user " +
           "AND (:category IS NULL OR :category = '' OR :category = 'All' OR r.category = :category) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "     LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(r.doctorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(r.notes) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY r.uploadedAt DESC")
    List<MedicalRecord> searchRecords(@Param("user") User user, @Param("query") String query, @Param("category") String category);
}
