package com.example.attendance.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

// ===========================
// 📌 ENTITY
// ===========================
@Entity
@Table(name = "student")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Student {
    @Id
    private String id;
    private String name;
    private String studentId; // e.g. Roll Number
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String faceDescriptor; // JSON String
}

// ===========================
// 📌 DTOs
// ===========================
@Data
class StudentDTO {
    private String id;
    private String name;
    private String studentId;
    private String email;
    private List<Double> faceDescriptor; // REST API communicates in Arrays
}

@Data
class CreateStudentDTO {
    private String name;
    private String studentId;
    private String email;
    private List<Double> faceDescriptor;
}

@Data
class UpdateStudentDTO {
    private String name;
    private String studentId;
    private String email;
    private List<Double> faceDescriptor;
}

// ===========================
// 📌 REPOSITORY
// ===========================
interface StudentRepository extends JpaRepository<Student, String> {}

// ===========================
// 📌 SERVICE
// ===========================
@Service
@RequiredArgsConstructor
class StudentService {
    private final StudentRepository repository;
    private final ObjectMapper objectMapper;

    public List<Student> getStudents() {
        return repository.findAll();
    }

    public Student createStudent(CreateStudentDTO data) {
        String faceDescJson = null;
        try {
            if (data.getFaceDescriptor() != null) {
                faceDescJson = objectMapper.writeValueAsString(data.getFaceDescriptor());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing face descriptor", e);
        }

        Student student = Student.builder()
                .id(UUID.randomUUID().toString())
                .name(data.getName())
                .studentId(data.getStudentId())
                .email(data.getEmail())
                .faceDescriptor(faceDescJson)
                .build();

        return repository.save(student);
    }

    public Student updateStudent(String id, UpdateStudentDTO updates) {
        return repository.findById(id).map(student -> {
            if (updates.getName() != null) student.setName(updates.getName());
            if (updates.getStudentId() != null) student.setStudentId(updates.getStudentId());
            if (updates.getEmail() != null) student.setEmail(updates.getEmail());
            
            if (updates.getFaceDescriptor() != null) {
                try {
                    student.setFaceDescriptor(objectMapper.writeValueAsString(updates.getFaceDescriptor()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error serializing face descriptor", e);
                }
            }
            return repository.save(student);
        }).orElse(null);
    }

    public boolean deleteStudent(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Student getStudentById(String id) {
        return repository.findById(id).orElse(null);
    }
    
    // Helper to convert Entity to DTO (useful for unpacking the JSON string back to array)
    public StudentDTO toDTO(Student student) {
        if (student == null) return null;
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setStudentId(student.getStudentId());
        dto.setEmail(student.getEmail());
        
        if (student.getFaceDescriptor() != null) {
            try {
                dto.setFaceDescriptor(objectMapper.readValue(student.getFaceDescriptor(), new TypeReference<List<Double>>(){}));
            } catch (JsonProcessingException e) {
                // Handle error or leave null
            }
        }
        return dto;
    }
}

// ===========================
// 📌 CONTROLLER
// ===========================
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
class StudentController {
    private final StudentService service;

    @GetMapping
    public List<StudentDTO> getAll() {
        return service.getStudents().stream().map(service::toDTO).toList();
    }

    @PostMapping
    public ResponseEntity<StudentDTO> create(@RequestBody CreateStudentDTO data) {
        return ResponseEntity.ok(service.toDTO(service.createStudent(data)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getById(@PathVariable String id) {
        Student student = service.getStudentById(id);
        return student != null ? ResponseEntity.ok(service.toDTO(student)) : ResponseEntity.notFound().build();
    }

    // CHANGED FROM @PatchMapping to @PutMapping to match Frontend Fetch Request
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> update(@PathVariable String id, @RequestBody UpdateStudentDTO updates) {
        Student student = service.updateStudent(id, updates);
        return student != null ? ResponseEntity.ok(service.toDTO(student)) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.deleteStudent(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}