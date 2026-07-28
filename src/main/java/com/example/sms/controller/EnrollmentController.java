package com.example.sms.controller;

import com.example.sms.model.Course;
import com.example.sms.model.Enrollment;
import com.example.sms.model.Student;
import com.example.sms.repository.CourseRepository;
import com.example.sms.repository.EnrollmentRepository;
import com.example.sms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Get all enrollments
    @GetMapping
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    // Enroll a student in a course
    @PostMapping
    public ResponseEntity<?> enrollStudent(@RequestBody Enrollment enrollmentRequest) {
        if (enrollmentRequest.getStudent() == null || enrollmentRequest.getStudent().getId() == null) {
            return ResponseEntity.badRequest().body("Student ID is required");
        }
        if (enrollmentRequest.getCourse() == null || enrollmentRequest.getCourse().getId() == null) {
            return ResponseEntity.badRequest().body("Course ID is required");
        }

        Optional<Student> studentOpt = studentRepository.findById(enrollmentRequest.getStudent().getId());
        Optional<Course> courseOpt = courseRepository.findById(enrollmentRequest.getCourse().getId());

        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Course not found");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            return ResponseEntity.badRequest().body("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        
        if (enrollmentRequest.getEnrollmentDate() == null) {
            enrollment.setEnrollmentDate(LocalDate.now());
        } else {
            enrollment.setEnrollmentDate(enrollmentRequest.getEnrollmentDate());
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return ResponseEntity.ok(savedEnrollment);
    }

    // Delete an enrollment (unenroll student)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long id) {
        if (!enrollmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        enrollmentRepository.deleteById(id);
        return ResponseEntity.ok("Enrollment removed successfully");
    }
}
