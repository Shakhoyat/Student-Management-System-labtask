package com.example.webapp.repository;

import com.example.webapp.entity.Teacher;
import com.example.webapp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByDepartment(Department department);
    List<Teacher> findByDepartmentId(Long departmentId);
    Optional<Teacher> findByEmail(String email);
}
