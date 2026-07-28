package com.example.sms.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String grade; // e.g., "A", "B+", "C", "F"

    private String remarks;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    // Constructors
    public Grade() {
    }

    public Grade(Student student, Course course, String grade, String remarks, LocalDate recordedDate) {
        this.student = student;
        this.course = course;
        this.grade = grade;
        this.remarks = remarks;
        this.recordedDate = recordedDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }
}
