package com.example.webapp.service;

import com.example.webapp.dto.RegisterDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.User;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// WHAT: Unit test for UserService (Authentication & Registration)
// HOW: Mocks UserRepository and PasswordEncoder
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encodedPassword"); // BCrypt encoded
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setConfirmPassword("password123");
        registerDTO.setName("New User");
        registerDTO.setEmail("new@example.com");
        registerDTO.setRole("STUDENT");
    }

    // ==================== TEST: existsByUsername ====================
    @Test
    void existsByUsername_WhenExists_ShouldReturnTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertTrue(userService.existsByUsername("testuser"));
        verify(userRepository, times(1)).existsByUsername("testuser");
    }

    @Test
    void existsByUsername_WhenNotExists_ShouldReturnFalse() {
        when(userRepository.existsByUsername("unknown")).thenReturn(false);

        assertFalse(userService.existsByUsername("unknown"));
    }

    // ==================== TEST: findByUsername ====================
    @Test
    void findByUsername_WhenFound_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_WhenNotFound_ShouldReturnEmpty() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("unknown");

        assertFalse(result.isPresent());
    }

    // ==================== TEST: findById ====================
    @Test
    void findById_WhenFound_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
    }

    // ==================== TEST: registerUser ====================
    // WHAT: Tests new user registration (password gets BCrypt hashed)
    @Test
    void registerUser_ShouldHashPasswordAndSave() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(registerDTO);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("password123"); // Password hashed
        verify(userRepository, times(1)).save(any(User.class));
    }

    // WHAT: Tests duplicate username registration
    @Test
    void registerUser_DuplicateUsername_ShouldThrowException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(registerDTO));
        verify(userRepository, never()).save(any(User.class)); // Save should NOT be called
    }

    // ==================== TEST: updateUser ====================
    @Test
    void updateUser_WhenFound_ShouldUpdate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, "Updated Name", "updated@email.com");

        assertNotNull(result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            userService.updateUser(99L, "Name", "email@test.com"));
    }

    // ==================== TEST: changePassword ====================
    @Test
    void changePassword_ShouldEncodeAndSave() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, "newPassword");

        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            userService.changePassword(99L, "newPassword"));
    }

    // ==================== TEST: checkPassword ====================
    // WHAT: Tests BCrypt password matching
    @Test
    void checkPassword_WhenMatch_ShouldReturnTrue() {
        when(passwordEncoder.matches("rawPassword", "$2a$10$encodedPassword")).thenReturn(true);

        assertTrue(userService.checkPassword("rawPassword", "$2a$10$encodedPassword"));
    }

    @Test
    void checkPassword_WhenNoMatch_ShouldReturnFalse() {
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertFalse(userService.checkPassword("wrongPassword", "$2a$10$encodedPassword"));
    }
}
