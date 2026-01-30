package com.example.webapp.repository;

import com.example.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRoll(String roll);
    Optional<Student> findByEmail(String email);
}
