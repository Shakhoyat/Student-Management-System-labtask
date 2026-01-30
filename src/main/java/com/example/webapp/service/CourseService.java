package com.example.webapp.service;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.entity.Course;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.CourseRepository;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository, 
                        DepartmentRepository departmentRepository,
                        StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<CourseDTO> getAllCoursesDTO() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public CourseDTO getCourseDTO(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDTO(course);
    }

    @Transactional
    public Course saveCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        
        if (courseDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            course.setDepartment(department);
        }
        
        Course savedCourse = courseRepository.save(course);
        
        // Handle student associations (Student owns the relationship)
        if (courseDTO.getStudentIds() != null && !courseDTO.getStudentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(courseDTO.getStudentIds());
            for (Student student : students) {
                student.getCourses().add(savedCourse);
                studentRepository.save(student);
            }
        }
        
        return savedCourse;
    }

    @Transactional
    public Course updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        
        if (courseDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            course.setDepartment(department);
        } else {
            course.setDepartment(null);
        }
        
        // Handle student associations (Student owns the ManyToMany relationship)
        // First, remove this course from all students who currently have it
        Set<Student> currentStudents = course.getStudents();
        if (currentStudents != null) {
            for (Student student : new HashSet<>(currentStudents)) {
                student.getCourses().remove(course);
                studentRepository.save(student);
            }
        }
        
        // Then add this course to the selected students
        if (courseDTO.getStudentIds() != null && !courseDTO.getStudentIds().isEmpty()) {
            List<Student> newStudents = studentRepository.findAllById(courseDTO.getStudentIds());
            for (Student student : newStudents) {
                student.getCourses().add(course);
                studentRepository.save(student);
            }
        }
        
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public List<CourseDTO> getCoursesByIds(List<Long> ids) {
        return courseRepository.findAllById(ids).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        if (course.getDepartment() != null) {
            dto.setDepartmentId(course.getDepartment().getId());
            dto.setDepartmentName(course.getDepartment().getName());
        }
        if (course.getStudents() != null) {
            dto.setStudentIds(course.getStudents().stream()
                    .map(s -> s.getId())
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
