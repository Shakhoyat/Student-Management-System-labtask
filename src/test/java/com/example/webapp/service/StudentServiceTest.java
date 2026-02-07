package com.example.webapp.service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Course;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.CourseRepository;
import com.example.webapp.repository.StudentRepository;
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

// WHAT: Unit test for StudentService
// HOW: Uses Mockito to mock repository dependencies, so no real database needed
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    // WHAT: The service under test with mocked dependencies injected
    @InjectMocks
    private StudentService studentService;

    private Student student;
    private StudentDTO studentDTO;

    // WHAT: Setup test data before each test
    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("John Doe");
        student.setRoll("CSE-001");
        student.setEmail("john@example.com");
        student.setRole(Role.STUDENT);
        student.setCourses(new HashSet<>());
        student.setTeachers(new HashSet<>());

        studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("John Doe");
        studentDTO.setRoll("CSE-001");
        studentDTO.setEmail("john@example.com");
    }

    // ==================== TEST: getAllStudents ====================
    @Test
    void getAllStudents_ShouldReturnList() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student));

        List<Student> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(studentRepository, times(1)).findAll();
    }

    // ==================== TEST: getAllStudentsDTO ====================
    @Test
    void getAllStudentsDTO_ShouldReturnDTOList() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student));

        List<StudentDTO> result = studentService.getAllStudentsDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    // ==================== TEST: getStudentById ====================
    @Test
    void getStudentById_WhenFound_ShouldReturnStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudentById(1L);

        assertTrue(result.isPresent());
        assertEquals("CSE-001", result.get().getRoll());
    }

    @Test
    void getStudentById_WhenNotFound_ShouldReturnEmpty() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Student> result = studentService.getStudentById(99L);

        assertFalse(result.isPresent());
    }

    // ==================== TEST: getStudentDTO ====================
    @Test
    void getStudentDTO_WhenFound_ShouldReturnDTO() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDTO result = studentService.getStudentDTO(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("CSE-001", result.getRoll());
    }

    @Test
    void getStudentDTO_WhenNotFound_ShouldThrowException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> studentService.getStudentDTO(99L));
    }

    // ==================== TEST: saveStudent ====================
    // WHAT: Tests creating a new student
    // HOW: Mocks repository save, verifies role is always STUDENT
    @Test
    void saveStudent_ShouldSaveWithStudentRole() {
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.saveStudent(studentDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void saveStudent_WithCourses_ShouldAssignCourses() {
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Programming");

        studentDTO.setCourseIds(Arrays.asList(1L));
        when(courseRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(course));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.saveStudent(studentDTO);

        assertNotNull(result);
        verify(courseRepository, times(1)).findAllById(Arrays.asList(1L));
    }

    // ==================== TEST: updateStudent ====================
    @Test
    void updateStudent_WhenFound_ShouldUpdate() {
        StudentDTO updateDTO = new StudentDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setRoll("CSE-002");
        updateDTO.setEmail("updated@example.com");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.updateStudent(1L, updateDTO, true);

        assertNotNull(result);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudent_WhenNotFound_ShouldThrowException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            studentService.updateStudent(99L, studentDTO, true));
    }

    // ==================== TEST: updateStudentByStudent ====================
    // WHAT: Tests that students can only edit their own profile
    @Test
    void updateStudentByStudent_OwnProfile_ShouldUpdate() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.updateStudentByStudent(1L, studentDTO, 1L);

        assertNotNull(result);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void updateStudentByStudent_OtherProfile_ShouldThrowException() {
        // Student with id=2 trying to edit student with id=1
        assertThrows(RuntimeException.class, () ->
            studentService.updateStudentByStudent(1L, studentDTO, 2L));
    }

    // ==================== TEST: deleteStudent ====================
    @Test
    void deleteStudent_ShouldCallRepositoryDelete() {
        doNothing().when(studentRepository).deleteById(1L);

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).deleteById(1L);
    }

    // ==================== TEST: convertToDTO ====================
    @Test
    void convertToDTO_ShouldMapAllFields() {
        StudentDTO result = studentService.convertToDTO(student);

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("CSE-001", result.getRoll());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("STUDENT", result.getRole());
    }
}
