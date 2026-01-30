package com.example.webapp.service;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    public TeacherService(TeacherRepository teacherRepository, 
                         DepartmentRepository departmentRepository,
                         StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public List<TeacherDTO> getAllTeachersDTO() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    public TeacherDTO getTeacherDTO(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return convertToDTO(teacher);
    }

    @Transactional
    public Teacher saveTeacher(TeacherDTO teacherDTO) {
        Teacher teacher = new Teacher();
        teacher.setName(teacherDTO.getName());
        teacher.setEmail(teacherDTO.getEmail());
        
        if (teacherDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(teacherDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            teacher.setDepartment(department);
        }
        
        if (teacherDTO.getStudentIds() != null && !teacherDTO.getStudentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(teacherDTO.getStudentIds());
            teacher.setStudents(new HashSet<>(students));
        }
        
        return teacherRepository.save(teacher);
    }

    @Transactional
    public Teacher updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        teacher.setName(teacherDTO.getName());
        teacher.setEmail(teacherDTO.getEmail());
        
        if (teacherDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(teacherDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            teacher.setDepartment(department);
        }
        
        if (teacherDTO.getStudentIds() != null) {
            List<Student> students = studentRepository.findAllById(teacherDTO.getStudentIds());
            teacher.setStudents(new HashSet<>(students));
        }
        
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }

    @Transactional
    public void assignStudentToTeacher(Long teacherId, Long studentId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        teacher.addStudent(student);
        teacherRepository.save(teacher);
    }

    public List<TeacherDTO> getTeachersByIds(List<Long> ids) {
        return teacherRepository.findAllById(ids).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setEmail(teacher.getEmail());
        if (teacher.getDepartment() != null) {
            dto.setDepartmentId(teacher.getDepartment().getId());
            dto.setDepartmentName(teacher.getDepartment().getName());
        }
        if (teacher.getStudents() != null) {
            dto.setStudentIds(teacher.getStudents().stream()
                    .map(Student::getId)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
