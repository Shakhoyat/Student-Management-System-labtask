package com.example.webapp.repository;

import com.example.webapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    public Optional<Student> findById(Long id);
    public List<Student> findAll();
    public Student save(Student student);
}
