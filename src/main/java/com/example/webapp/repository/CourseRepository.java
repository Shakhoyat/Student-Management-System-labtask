package com.example.webapp.repository;

import com.example.webapp.entity.Course;
import com.example.webapp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartment(Department department);
    List<Course> findByDepartmentId(Long departmentId);
}
