package com.example.webapp.service;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// WHAT: Unit test for TeacherService
// HOW: Mocks TeacherRepository, DepartmentRepository, StudentRepository
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher;
    private TeacherDTO teacherDTO;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setName("Dr. Smith");
        teacher.setEmail("smith@uni.edu");
        teacher.setRole(Role.TEACHER);
        teacher.setDepartment(department);
        teacher.setStudents(new HashSet<>());

        teacherDTO = new TeacherDTO();
        teacherDTO.setId(1L);
        teacherDTO.setName("Dr. Smith");
        teacherDTO.setEmail("smith@uni.edu");
        teacherDTO.setDepartmentId(1L);
    }

    // ==================== TEST: getAllTeachers ====================
    @Test
    void getAllTeachers_ShouldReturnList() {
        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher));

        List<Teacher> result = teacherService.getAllTeachers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        verify(teacherRepository, times(1)).findAll();
    }

    // ==================== TEST: getAllTeachersDTO ====================
    @Test
    void getAllTeachersDTO_ShouldReturnDTOList() {
        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher));

        List<TeacherDTO> result = teacherService.getAllTeachersDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        assertEquals("Computer Science", result.get(0).getDepartmentName());
    }

    // ==================== TEST: getTeacherById ====================
    @Test
    void getTeacherById_WhenFound_ShouldReturnTeacher() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        Optional<Teacher> result = teacherService.getTeacherById(1L);

        assertTrue(result.isPresent());
        assertEquals("Dr. Smith", result.get().getName());
    }

    @Test
    void getTeacherById_WhenNotFound_ShouldReturnEmpty() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Teacher> result = teacherService.getTeacherById(99L);

        assertFalse(result.isPresent());
    }

    // ==================== TEST: getTeacherDTO ====================
    @Test
    void getTeacherDTO_WhenFound_ShouldReturnDTO() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        TeacherDTO result = teacherService.getTeacherDTO(1L);

        assertNotNull(result);
        assertEquals("Dr. Smith", result.getName());
        assertEquals(1L, result.getDepartmentId());
    }

    @Test
    void getTeacherDTO_WhenNotFound_ShouldThrowException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teacherService.getTeacherDTO(99L));
    }

    // ==================== TEST: saveTeacher ====================
    @Test
    void saveTeacher_ShouldSaveAndReturn() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        Teacher result = teacherService.saveTeacher(teacherDTO);

        assertNotNull(result);
        assertEquals("Dr. Smith", result.getName());
        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void saveTeacher_WithInvalidDepartment_ShouldThrowException() {
        teacherDTO.setDepartmentId(99L);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teacherService.saveTeacher(teacherDTO));
    }

    @Test
    void saveTeacher_WithStudents_ShouldAssignStudents() {
        Student student = new Student();
        student.setId(1L);
        student.setTeachers(new HashSet<>());

        teacherDTO.setStudentIds(Arrays.asList(1L));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(student));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        Teacher result = teacherService.saveTeacher(teacherDTO);

        assertNotNull(result);
        verify(studentRepository, times(1)).findAllById(Arrays.asList(1L));
    }

    // ==================== TEST: updateTeacher ====================
    @Test
    void updateTeacher_WhenFound_ShouldUpdate() {
        TeacherDTO updateDTO = new TeacherDTO();
        updateDTO.setName("Dr. Updated");
        updateDTO.setEmail("updated@uni.edu");
        updateDTO.setDepartmentId(1L);

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        Teacher result = teacherService.updateTeacher(1L, updateDTO);

        assertNotNull(result);
        verify(teacherRepository, times(1)).save(teacher);
    }

    @Test
    void updateTeacher_WhenNotFound_ShouldThrowException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> teacherService.updateTeacher(99L, teacherDTO));
    }

    // ==================== TEST: deleteTeacher ====================
    @Test
    void deleteTeacher_ShouldCallRepositoryDelete() {
        doNothing().when(teacherRepository).deleteById(1L);

        teacherService.deleteTeacher(1L);

        verify(teacherRepository, times(1)).deleteById(1L);
    }

    // ==================== TEST: assignStudentToTeacher ====================
    @Test
    void assignStudentToTeacher_ShouldAssign() {
        Student student = new Student();
        student.setId(1L);
        student.setTeachers(new HashSet<>());

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);

        teacherService.assignStudentToTeacher(1L, 1L);

        verify(teacherRepository, times(1)).save(teacher);
    }

    @Test
    void assignStudentToTeacher_TeacherNotFound_ShouldThrow() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            teacherService.assignStudentToTeacher(99L, 1L));
    }
}
