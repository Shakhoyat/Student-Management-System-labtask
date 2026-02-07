package com.example.webapp.service;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.entity.Course;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.CourseRepository;
import com.example.webapp.repository.DepartmentRepository;
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

// WHAT: Unit test for CourseService
// HOW: Mocks CourseRepository, DepartmentRepository, StudentRepository
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private CourseDTO courseDTO;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        course = new Course();
        course.setId(1L);
        course.setName("Java Programming");
        course.setDescription("Learn Java basics");
        course.setDepartment(department);
        course.setStudents(new HashSet<>());

        courseDTO = new CourseDTO();
        courseDTO.setId(1L);
        courseDTO.setName("Java Programming");
        courseDTO.setDescription("Learn Java basics");
        courseDTO.setDepartmentId(1L);
    }

    // ==================== TEST: getAllCourses ====================
    @Test
    void getAllCourses_ShouldReturnList() {
        when(courseRepository.findAll()).thenReturn(Arrays.asList(course));

        List<Course> result = courseService.getAllCourses();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getName());
        verify(courseRepository, times(1)).findAll();
    }

    // ==================== TEST: getAllCoursesDTO ====================
    @Test
    void getAllCoursesDTO_ShouldReturnDTOList() {
        when(courseRepository.findAll()).thenReturn(Arrays.asList(course));

        List<CourseDTO> result = courseService.getAllCoursesDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getName());
        assertEquals("Computer Science", result.get(0).getDepartmentName());
    }

    // ==================== TEST: getCourseById ====================
    @Test
    void getCourseById_WhenFound_ShouldReturnCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Optional<Course> result = courseService.getCourseById(1L);

        assertTrue(result.isPresent());
        assertEquals("Java Programming", result.get().getName());
    }

    @Test
    void getCourseById_WhenNotFound_ShouldReturnEmpty() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Course> result = courseService.getCourseById(99L);

        assertFalse(result.isPresent());
    }

    // ==================== TEST: getCourseDTO ====================
    @Test
    void getCourseDTO_WhenFound_ShouldReturnDTO() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseDTO result = courseService.getCourseDTO(1L);

        assertNotNull(result);
        assertEquals("Java Programming", result.getName());
        assertEquals("Learn Java basics", result.getDescription());
        assertEquals(1L, result.getDepartmentId());
    }

    @Test
    void getCourseDTO_WhenNotFound_ShouldThrowException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> courseService.getCourseDTO(99L));
    }

    // ==================== TEST: saveCourse ====================
    @Test
    void saveCourse_ShouldSaveAndReturn() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course result = courseService.saveCourse(courseDTO);

        assertNotNull(result);
        assertEquals("Java Programming", result.getName());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void saveCourse_WithInvalidDepartment_ShouldThrowException() {
        courseDTO.setDepartmentId(99L);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> courseService.saveCourse(courseDTO));
    }

    @Test
    void saveCourse_WithStudents_ShouldAssignStudents() {
        Student student = new Student();
        student.setId(1L);
        student.setCourses(new HashSet<>());

        courseDTO.setStudentIds(Arrays.asList(1L));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(studentRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Course result = courseService.saveCourse(courseDTO);

        assertNotNull(result);
        verify(studentRepository, times(1)).findAllById(Arrays.asList(1L));
    }

    // ==================== TEST: updateCourse ====================
    @Test
    void updateCourse_WhenFound_ShouldUpdate() {
        CourseDTO updateDTO = new CourseDTO();
        updateDTO.setName("Advanced Java");
        updateDTO.setDescription("Updated description");
        updateDTO.setDepartmentId(1L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course result = courseService.updateCourse(1L, updateDTO);

        assertNotNull(result);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void updateCourse_WhenNotFound_ShouldThrowException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> courseService.updateCourse(99L, courseDTO));
    }

    // ==================== TEST: deleteCourse ====================
    @Test
    void deleteCourse_ShouldCallRepositoryDelete() {
        doNothing().when(courseRepository).deleteById(1L);

        courseService.deleteCourse(1L);

        verify(courseRepository, times(1)).deleteById(1L);
    }

    // ==================== TEST: getCoursesByIds ====================
    @Test
    void getCoursesByIds_ShouldReturnDTOList() {
        when(courseRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(course));

        List<CourseDTO> result = courseService.getCoursesByIds(Arrays.asList(1L));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Programming", result.get(0).getName());
    }
}
