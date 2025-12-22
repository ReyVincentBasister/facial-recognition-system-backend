package com.example.attendance.attendance;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ===========================
// 📌 ENTITY
// ===========================
@Entity
@Table(name = "attendance_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AttendanceLog {
    @Id
    private String id;
    private String studentId;
    private String eventId;
    private String confidence; // Stored as String to match schema, parsed later
    private String status; // "present" | "late" | "absent"
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}