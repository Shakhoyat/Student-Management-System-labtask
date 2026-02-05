package com.example.webapp.service;

import com.example.webapp.dto.DepartmentDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// WHAT: Unit test class for DepartmentService
// HOW: @ExtendWith(MockitoExtension.class) enables Mockito annotations like @Mock and @InjectMocks
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    // WHAT: Mock object - fake implementation of repository
    // HOW: Mockito creates a fake DepartmentRepository, so we don't need real database
    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ModelMapper modelMapper;

    // WHAT: The actual service we're testing
    // HOW: @InjectMocks injects the @Mock objects into this service automatically
    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private DepartmentDTO departmentDTO;

    // WHAT: Runs before each test method
    // HOW: Sets up test data that will be used in multiple tests
    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        departmentDTO = new DepartmentDTO();
        departmentDTO.setId(1L);
        departmentDTO.setName("Computer Science");
    }

    // ==================== TEST: getAllDepartments ====================
    // WHAT: Tests if getAllDepartments returns list of departments
    // HOW: Mock repository.findAll() to return fake data, verify service returns it correctly
    @Test
    void getAllDepartments_ShouldReturnListOfDepartments() {
        // Arrange: Setup mock behavior - when findAll() is called, return this list
        List<Department> departments = Arrays.asList(department);
        when(departmentRepository.findAll()).thenReturn(departments);

        // Act: Call the actual service method
        List<Department> result = departmentService.getAllDepartments();

        // Assert: Verify the result is correct
        assertNotNull(result);                          // Result should not be null
        assertEquals(1, result.size());                 // Should have 1 department
        assertEquals("Computer Science", result.get(0).getName()); // Name should match
        
        // Verify: Check that repository method was called exactly once
        verify(departmentRepository, times(1)).findAll();
    }

    // ==================== TEST: getDepartmentById ====================
    // WHAT: Tests if getDepartmentById returns correct department when found
    // HOW: Mock findById() to return Optional with department
    @Test
    void getDepartmentById_WhenFound_ShouldReturnDepartment() {
        // Arrange
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // Act
        Optional<Department> result = departmentService.getDepartmentById(1L);

        // Assert
        assertTrue(result.isPresent());                          // Optional should have value
        assertEquals("Computer Science", result.get().getName()); // Name should match
        verify(departmentRepository, times(1)).findById(1L);
    }

    // WHAT: Tests getDepartmentById when department doesn't exist
    // HOW: Mock findById() to return empty Optional
    @Test
    void getDepartmentById_WhenNotFound_ShouldReturnEmpty() {
        // Arrange
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Department> result = departmentService.getDepartmentById(99L);

        // Assert
        assertFalse(result.isPresent()); // Optional should be empty
    }

    // ==================== TEST: saveDepartment ====================
    // WHAT: Tests if saveDepartment correctly saves a new department
    // HOW: Mock modelMapper and repository.save(), verify they're called correctly
    @Test
    void saveDepartment_ShouldSaveAndReturnDepartment() {
        // Arrange
        when(modelMapper.map(departmentDTO, Department.class)).thenReturn(department);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // Act
        Department result = departmentService.saveDepartment(departmentDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    // ==================== TEST: updateDepartment ====================
    // WHAT: Tests if updateDepartment correctly updates existing department
    // HOW: Mock findById() and save(), verify name is updated
    @Test
    void updateDepartment_WhenFound_ShouldUpdateAndReturn() {
        // Arrange
        DepartmentDTO updateDTO = new DepartmentDTO();
        updateDTO.setName("Updated Name");
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // Act
        Department result = departmentService.updateDepartment(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(departmentRepository, times(1)).save(department);
    }

    // WHAT: Tests updateDepartment when department doesn't exist
    // HOW: Mock findById() to return empty, expect RuntimeException
    @Test
    void updateDepartment_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: Verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            departmentService.updateDepartment(99L, departmentDTO);
        });
    }

    // ==================== TEST: deleteDepartment ====================
    // WHAT: Tests if deleteDepartment calls repository delete method
    // HOW: Just verify that deleteById() is called with correct id
    @Test
    void deleteDepartment_ShouldCallRepositoryDelete() {
        // Arrange: doNothing() because delete returns void
        doNothing().when(departmentRepository).deleteById(1L);

        // Act
        departmentService.deleteDepartment(1L);

        // Assert: Verify delete was called exactly once with id=1
        verify(departmentRepository, times(1)).deleteById(1L);
    }
}
