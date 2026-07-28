package com.example.sms.repository;

import com.example.sms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
}
