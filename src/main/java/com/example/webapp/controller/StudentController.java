package com.example.webapp.controller;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.security.CustomUserDetails;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final TeacherService teacherService;

    public StudentController(StudentService studentService, 
                            CourseService courseService,
                            TeacherService teacherService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.teacherService = teacherService;
    }

    @GetMapping
    public String getAllStudents(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("students", studentService.getAllStudentsDTO());
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getProfileId());
        }
        return "students";
    }

    @GetMapping("/{id}")
    public String viewStudent(@PathVariable Long id, Model model) {
        StudentDTO student = studentService.getStudentDTO(id);
        model.addAttribute("student", student);
        if (student.getTeacherIds() != null && !student.getTeacherIds().isEmpty()) {
            model.addAttribute("teachers", teacherService.getTeachersByIds(student.getTeacherIds()));
        }
        if (student.getCourseIds() != null && !student.getCourseIds().isEmpty()) {
            model.addAttribute("courses", courseService.getCoursesByIds(student.getCourseIds()));
        }
        return "student-view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('TEACHER')")
    public String showAddForm(Model model) {
        model.addAttribute("student", new StudentDTO());
        model.addAttribute("teachers", teacherService.getAllTeachersDTO());
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "student-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public String createStudent(@ModelAttribute("student") StudentDTO studentDTO,
                               RedirectAttributes redirectAttributes) {
        studentService.saveStudent(studentDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Student created successfully");
        return "redirect:/students";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and @securityService.isOwnProfile(#id, authentication))")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentDTO(id));
        model.addAttribute("teachers", teacherService.getAllTeachersDTO());
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "student-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and @securityService.isOwnProfile(#id, authentication))")
    public String updateStudent(@PathVariable Long id,
                               @ModelAttribute("student") StudentDTO studentDTO,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        boolean isTeacher = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        
        if (isTeacher) {
            studentService.updateStudent(id, studentDTO, true);
        } else {
            // Student can only edit their own profile and cannot change role
            studentService.updateStudentByStudent(id, studentDTO, userDetails.getProfileId());
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully");
        return "redirect:/students";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteStudent(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully");
        return "redirect:/students";
    }
}

