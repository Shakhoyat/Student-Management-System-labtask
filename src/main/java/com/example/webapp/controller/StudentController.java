package com.example.webapp.controller;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import jakarta.servlet.http.HttpSession;
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
    public String getAllStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudentsDTO());
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
    public String showAddForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can create student profiles");
            return "redirect:/students";
        }
        model.addAttribute("student", new StudentDTO());
        model.addAttribute("teachers", teacherService.getAllTeachersDTO());
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "student-form";
    }

    @PostMapping
    public String createStudent(@ModelAttribute("student") StudentDTO studentDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can create student profiles");
            return "redirect:/students";
        }
        studentService.saveStudent(studentDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Student created successfully");
        return "redirect:/students";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");
        
        // Students can only edit their own profile
        if (userRole == Role.STUDENT && !id.equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own profile");
            return "redirect:/students";
        }
        
        model.addAttribute("student", studentService.getStudentDTO(id));
        model.addAttribute("teachers", teacherService.getAllTeachersDTO());
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "student-form";
    }

    @PostMapping("/{id}/edit")
    public String updateStudent(@PathVariable Long id,
                               @ModelAttribute("student") StudentDTO studentDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");
        
        if (userRole == Role.STUDENT) {
            // Student can only edit their own profile
            if (!id.equals(userId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own profile");
                return "redirect:/students";
            }
            // Student cannot change role - use special update method
            studentService.updateStudentByStudent(id, studentDTO, userId);
        } else {
            // Teacher can edit any student including role
            studentService.updateStudent(id, studentDTO, true);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully");
        return "redirect:/students";
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can delete students");
            return "redirect:/students";
        }
        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully");
        return "redirect:/students";
    }
}

