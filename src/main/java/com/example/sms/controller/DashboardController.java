package com.example.sms.controller;

import com.example.sms.model.*;
import com.example.sms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/stats")
    public DashboardStats getDashboardStats() {
        long totalStudents = studentRepository.count();
        long totalCourses = courseRepository.count();

        // Calculate Average GPA
        List<Grade> grades = gradeRepository.findAll();
        double averageGpa = 0.0;
        if (!grades.isEmpty()) {
            double totalPoints = 0.0;
            int count = 0;
            for (Grade g : grades) {
                Double points = convertToGpaPoints(g.getGrade());
                if (points != null) {
                    totalPoints += points;
                    count++;
                }
            }
            averageGpa = count > 0 ? totalPoints / count : 0.0;
        }

        // Calculate Attendance Rate
        List<Attendance> attendanceRecords = attendanceRepository.findAll();
        double attendanceRate = 0.0;
        if (!attendanceRecords.isEmpty()) {
            long presentCount = attendanceRecords.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase("PRESENT") || a.getStatus().equalsIgnoreCase("LATE"))
                    .count();
            attendanceRate = ((double) presentCount / attendanceRecords.size()) * 100;
        }

        return new DashboardStats(totalStudents, totalCourses, averageGpa, attendanceRate);
    }

    private Double convertToGpaPoints(String gradeStr) {
        if (gradeStr == null) return null;
        
        // Try parsing numeric grade first (e.g. 0 to 100)
        try {
            double numericGrade = Double.parseDouble(gradeStr.trim());
            if (numericGrade >= 90) return 4.0;
            if (numericGrade >= 80) return 3.0;
            if (numericGrade >= 70) return 2.0;
            if (numericGrade >= 60) return 1.0;
            return 0.0;
        } catch (NumberFormatException e) {
            // It's a letter grade
            String g = gradeStr.toUpperCase().trim();
            switch (g) {
                case "A+":
                case "A":   return 4.0;
                case "A-":  return 3.7;
                case "B+":  return 3.3;
                case "B":   return 3.0;
                case "B-":  return 2.7;
                case "C+":  return 2.3;
                case "C":   return 2.0;
                case "C-":  return 1.7;
                case "D+":  return 1.3;
                case "D":   return 1.0;
                case "F":   return 0.0;
                default:    return null; // Ignore invalid grades in average calculation
            }
        }
    }

    // Response DTO
    public static class DashboardStats {
        private long totalStudents;
        private long totalCourses;
        private double averageGpa;
        private double attendanceRate;

        public DashboardStats(long totalStudents, long totalCourses, double averageGpa, double attendanceRate) {
            this.totalStudents = totalStudents;
            this.totalCourses = totalCourses;
            this.averageGpa = averageGpa;
            this.attendanceRate = attendanceRate;
        }

        // Getters and Setters
        public long getTotalStudents() {
            return totalStudents;
        }

        public void setTotalStudents(long totalStudents) {
            this.totalStudents = totalStudents;
        }

        public long getTotalCourses() {
            return totalCourses;
        }

        public void setTotalCourses(long totalCourses) {
            this.totalCourses = totalCourses;
        }

        public double getAverageGpa() {
            return averageGpa;
        }

        public void setAverageGpa(double averageGpa) {
            this.averageGpa = averageGpa;
        }

        public double getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(double attendanceRate) {
            this.attendanceRate = attendanceRate;
        }
    }
}
