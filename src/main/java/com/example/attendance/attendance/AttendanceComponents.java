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

@Data
class AttendanceDTO {
    private String studentId;
    private String eventId;
    private Double confidence;
    private String status;
}
interface AttendanceRepository extends JpaRepository<AttendanceLog, String> {
    Optional<AttendanceLog> findByStudentIdAndEventId(String studentId, String eventId);
    List<AttendanceLog> findByEventId(String eventId);
    List<AttendanceLog> findByStudentId(String studentId);
    List<AttendanceLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}

// ===========================
// 📌 SERVICE
// ===========================
@Service
@RequiredArgsConstructor
class AttendanceService {
    private final AttendanceRepository repository;

    public List<AttendanceLog> getAllLogs() {
        return repository.findAll();
    }

    public AttendanceLog saveAttendanceLog(AttendanceDTO data) {
        // Prevent duplicate attendance for the same event
        Optional<AttendanceLog> existing = repository.findByStudentIdAndEventId(data.getStudentId(), data.getEventId());
        
        if (existing.isPresent()) {
            return existing.get();
        }

        AttendanceLog newLog = AttendanceLog.builder()
                .id(UUID.randomUUID().toString())
                .studentId(data.getStudentId())
                .eventId(data.getEventId())
                .confidence(String.valueOf(data.getConfidence()))
                .status(data.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        return repository.save(newLog);
    }

    public List<AttendanceLog> getAttendanceByEvent(String eventId) {
        return repository.findByEventId(eventId);
    }

    public AttendanceLog getAttendanceLogById(String id) {
        return repository.findById(id).orElse(null);
    }
    
    public List<AttendanceLog> getAttendanceByStudent(String studentId) {
        return repository.findByStudentId(studentId);
    }

    public List<AttendanceLog> getAttendanceByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end);
    }
}
// ===========================
// 📌 CONTROLLER
// ===========================
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
class AttendanceController {
    private final AttendanceService service;

    @GetMapping
    public List<AttendanceLog> getAll() {
        return service.getAllLogs();
    }
    @PostMapping
    public ResponseEntity<AttendanceLog> create(@RequestBody AttendanceDTO data) {
        return ResponseEntity.ok(service.saveAttendanceLog(data));
    }

}