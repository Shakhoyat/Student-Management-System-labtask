package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Course;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.repository.CourseRepository;
import com.example.webapp.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<StudentDTO> getAllStudentsDTO() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public StudentDTO getStudentDTO(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return convertToDTO(student);
    }

    @Transactional
    public Student saveStudent(StudentDTO studentDTO) {
        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setRoll(studentDTO.getRoll());
        student.setEmail(studentDTO.getEmail());
        student.setRole(Role.STUDENT); // Always set role to STUDENT when creating
        
        if (studentDTO.getCourseIds() != null && !studentDTO.getCourseIds().isEmpty()) {
            List<Course> courses = courseRepository.findAllById(studentDTO.getCourseIds());
            student.setCourses(new HashSet<>(courses));
        }
        
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, StudentDTO studentDTO, boolean isTeacher) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Students can edit everything except role
        student.setName(studentDTO.getName());
        student.setRoll(studentDTO.getRoll());
        student.setEmail(studentDTO.getEmail());
        
        // Only teachers can change role (but we keep it as STUDENT always for students)
        // Role remains unchanged for student self-edit
        
        if (studentDTO.getCourseIds() != null) {
            List<Course> courses = courseRepository.findAllById(studentDTO.getCourseIds());
            student.setCourses(new HashSet<>(courses));
        }
        
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudentByStudent(Long id, StudentDTO studentDTO, Long currentStudentId) {
        // Student can only edit their own profile
        if (!id.equals(currentStudentId)) {
            throw new RuntimeException("Students can only edit their own profile");
        }
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Student can edit everything EXCEPT role
        student.setName(studentDTO.getName());
        student.setRoll(studentDTO.getRoll());
        student.setEmail(studentDTO.getEmail());
        // Role is NOT updated - student cannot change their role
        
        if (studentDTO.getCourseIds() != null) {
            List<Course> courses = courseRepository.findAllById(studentDTO.getCourseIds());
            student.setCourses(new HashSet<>(courses));
        }
        
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<StudentDTO> getStudentsByIds(List<Long> ids) {
        return studentRepository.findAllById(ids).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setRoll(student.getRoll());
        dto.setEmail(student.getEmail());
        dto.setRole(student.getRole().name());
        if (student.getCourses() != null) {
            dto.setCourseIds(student.getCourses().stream()
                    .map(Course::getId)
                    .collect(Collectors.toList()));
        }
        if (student.getTeachers() != null) {
            dto.setTeacherIds(student.getTeachers().stream()
                    .map(Teacher::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
