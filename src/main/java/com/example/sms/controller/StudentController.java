package com.example.sms.controller;

import com.example.sms.model.Student;
import com.example.sms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Get all students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new student
    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Generate unique student ID: SMS-2026-XXXX
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            long count = studentRepository.count() + 1;
            String generatedId = String.format("SMS-2026-%04d", count);
            // Ensure uniqueness
            while (studentRepository.existsByStudentId(generatedId)) {
                count++;
                generatedId = String.format("SMS-2026-%04d", count);
            }
            student.setStudentId(generatedId);
        } else if (studentRepository.existsByStudentId(student.getStudentId())) {
            return ResponseEntity.badRequest().body("Student ID already exists");
        }

        if (student.getEnrollmentDate() == null) {
            student.setEnrollmentDate(LocalDate.now());
        }

        Student savedStudent = studentRepository.save(student);
        return ResponseEntity.ok(savedStudent);
    }

    // Update a student
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = optionalStudent.get();
        
        // If email changed, verify new email is unique
        if (!student.getEmail().equals(studentDetails.getEmail()) && studentRepository.existsByEmail(studentDetails.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        student.setFirstName(studentDetails.getFirstName());
        student.setLastName(studentDetails.getLastName());
        student.setEmail(studentDetails.getEmail());
        student.setPhoneNumber(studentDetails.getPhoneNumber());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        
        if (studentDetails.getEnrollmentDate() != null) {
            student.setEnrollmentDate(studentDetails.getEnrollmentDate());
        }

        Student updatedStudent = studentRepository.save(student);
        return ResponseEntity.ok(updatedStudent);
    }

    // Delete a student (clean up enrollments, grades, attendance in transactional manner)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Clean up linked data to prevent foreign key constraint issues
        enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(id))
                .forEach(e -> enrollmentRepository.delete(e));

        gradeRepository.findAll().stream()
                .filter(g -> g.getStudent().getId().equals(id))
                .forEach(g -> gradeRepository.delete(g));

        attendanceRepository.findAll().stream()
                .filter(a -> a.getStudent().getId().equals(id))
                .forEach(a -> attendanceRepository.delete(a));

        studentRepository.deleteById(id);
        return ResponseEntity.ok("Student deleted successfully");
    }
}
