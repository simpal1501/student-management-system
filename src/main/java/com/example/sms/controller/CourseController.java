package com.example.sms.controller;

import com.example.sms.model.Course;
import com.example.sms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Get all courses
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Get course by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);
        return course.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new course
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            return ResponseEntity.badRequest().body("Course code already exists");
        }

        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    // Update a course
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // If code changed, check uniqueness
        if (!course.getCourseCode().equals(courseDetails.getCourseCode()) && courseRepository.existsByCourseCode(courseDetails.getCourseCode())) {
            return ResponseEntity.badRequest().body("Course code already exists");
        }

        course.setCourseCode(courseDetails.getCourseCode());
        course.setCourseName(courseDetails.getCourseName());
        course.setDescription(courseDetails.getDescription());
        course.setCredits(courseDetails.getCredits());

        Course updatedCourse = courseRepository.save(course);
        return ResponseEntity.ok(updatedCourse);
    }

    // Delete a course
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Clean up linked data to prevent foreign key constraint issues
        enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(id))
                .forEach(e -> enrollmentRepository.delete(e));

        gradeRepository.findAll().stream()
                .filter(g -> g.getCourse().getId().equals(id))
                .forEach(g -> gradeRepository.delete(g));

        attendanceRepository.findAll().stream()
                .filter(a -> a.getCourse().getId().equals(id))
                .forEach(a -> attendanceRepository.delete(a));

        courseRepository.deleteById(id);
        return ResponseEntity.ok("Course deleted successfully");
    }
}
