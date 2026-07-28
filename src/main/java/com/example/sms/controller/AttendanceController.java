package com.example.sms.controller;

import com.example.sms.model.*;
import com.example.sms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // Get all attendance records
    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    // Submit or update an attendance record
    @PostMapping
    public ResponseEntity<?> recordAttendance(@RequestBody Attendance attendanceRequest) {
        if (attendanceRequest.getStudent() == null || attendanceRequest.getStudent().getId() == null) {
            return ResponseEntity.badRequest().body("Student ID is required");
        }
        if (attendanceRequest.getCourse() == null || attendanceRequest.getCourse().getId() == null) {
            return ResponseEntity.badRequest().body("Course ID is required");
        }
        if (attendanceRequest.getStatus() == null || attendanceRequest.getStatus().isEmpty()) {
            return ResponseEntity.badRequest().body("Attendance status is required");
        }

        Optional<Student> studentOpt = studentRepository.findById(attendanceRequest.getStudent().getId());
        Optional<Course> courseOpt = courseRepository.findById(attendanceRequest.getCourse().getId());

        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Course not found");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        // Business Logic: Must be enrolled to have attendance recorded
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            return ResponseEntity.badRequest().body("Cannot record attendance. Student is not enrolled in this course");
        }

        LocalDate attendanceDate = attendanceRequest.getAttendanceDate();
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }

        // Validate status value
        String status = attendanceRequest.getStatus().toUpperCase();
        if (!status.equals("PRESENT") && !status.equals("ABSENT") && !status.equals("LATE")) {
            return ResponseEntity.badRequest().body("Status must be PRESENT, ABSENT, or LATE");
        }

        // Upsert logic: If attendance record already exists for student + course + date, update it.
        Optional<Attendance> existingAttendanceOpt = attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(
                student.getId(), course.getId(), attendanceDate);
        
        Attendance attendanceToSave;
        if (existingAttendanceOpt.isPresent()) {
            attendanceToSave = existingAttendanceOpt.get();
            attendanceToSave.setStatus(status);
        } else {
            attendanceToSave = new Attendance();
            attendanceToSave.setStudent(student);
            attendanceToSave.setCourse(course);
            attendanceToSave.setAttendanceDate(attendanceDate);
            attendanceToSave.setStatus(status);
        }

        Attendance savedAttendance = attendanceRepository.save(attendanceToSave);
        return ResponseEntity.ok(savedAttendance);
    }

    // Delete attendance record
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        if (!attendanceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        attendanceRepository.deleteById(id);
        return ResponseEntity.ok("Attendance record deleted successfully");
    }
}
