package com.example.sms.controller;

import com.example.sms.model.Course;
import com.example.sms.model.Grade;
import com.example.sms.model.Student;
import com.example.sms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Get all grades
    @GetMapping
    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }

    // Submit or update a grade (upsert)
    @PostMapping
    public ResponseEntity<?> recordGrade(@RequestBody Grade gradeRequest) {
        if (gradeRequest.getStudent() == null || gradeRequest.getStudent().getId() == null) {
            return ResponseEntity.badRequest().body("Student ID is required");
        }
        if (gradeRequest.getCourse() == null || gradeRequest.getCourse().getId() == null) {
            return ResponseEntity.badRequest().body("Course ID is required");
        }
        if (gradeRequest.getGrade() == null || gradeRequest.getGrade().isEmpty()) {
            return ResponseEntity.badRequest().body("Grade is required");
        }

        Optional<Student> studentOpt = studentRepository.findById(gradeRequest.getStudent().getId());
        Optional<Course> courseOpt = courseRepository.findById(gradeRequest.getCourse().getId());

        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Course not found");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        // Business Logic: Must be enrolled to be graded
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            return ResponseEntity.badRequest().body("Cannot record grade. Student is not enrolled in this course");
        }

        // Upsert logic: If grade already exists for this student & course, update it. Otherwise, create a new record.
        Optional<Grade> existingGradeOpt = gradeRepository.findByStudentIdAndCourseId(student.getId(), course.getId());
        Grade gradeToSave;
        
        if (existingGradeOpt.isPresent()) {
            gradeToSave = existingGradeOpt.get();
            gradeToSave.setGrade(gradeRequest.getGrade());
            gradeToSave.setRemarks(gradeRequest.getRemarks());
            gradeToSave.setRecordedDate(LocalDate.now());
        } else {
            gradeToSave = new Grade();
            gradeToSave.setStudent(student);
            gradeToSave.setCourse(course);
            gradeToSave.setGrade(gradeRequest.getGrade());
            gradeToSave.setRemarks(gradeRequest.getRemarks());
            gradeToSave.setRecordedDate(LocalDate.now());
        }

        Grade savedGrade = gradeRepository.save(gradeToSave);
        return ResponseEntity.ok(savedGrade);
    }

    // Delete a grade record
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id) {
        if (!gradeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        gradeRepository.deleteById(id);
        return ResponseEntity.ok("Grade record deleted successfully");
    }
}
