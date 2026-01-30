package com.example.webapp.controller;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

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
    public String getStudents(Model model, HttpSession session) {
        model.addAttribute("students", studentService.getAllStudentsDTO());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        model.addAttribute("currentRole", session.getAttribute("currentRole"));
        return "students";
    }

    // Only Teachers can add students
    @GetMapping("/add")
    public String addStudent(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Role currentRole = (Role) session.getAttribute("currentRole");
        if (currentRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("error", "Only teachers can create student profiles!");
            return "redirect:/students";
        }
        model.addAttribute("student", new StudentDTO());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("isEdit", false);
        return "student-form";
    }

    // Only Teachers can store/create students
    @PostMapping("/store")
    public String storeStudent(@ModelAttribute("student") StudentDTO studentDTO, 
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role currentRole = (Role) session.getAttribute("currentRole");
        if (currentRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("error", "Only teachers can create student profiles!");
            return "redirect:/students";
        }
        studentService.saveStudent(studentDTO);
        redirectAttributes.addFlashAttribute("success", "Student created successfully!");
        return "redirect:/students";
    }

    // Students can edit their own profile (everything except role)
    // Teachers can edit any student
    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model, HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        Role currentRole = (Role) session.getAttribute("currentRole");
        Long currentUserId = (Long) session.getAttribute("currentUserId");
        
        // Students can only edit their own profile
        if (currentRole == Role.STUDENT && !id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You can only edit your own profile!");
            return "redirect:/students";
        }
        
        model.addAttribute("student", studentService.getStudentDTO(id));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("isEdit", true);
        model.addAttribute("isStudent", currentRole == Role.STUDENT);
        return "student-form";
    }

    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id, 
                                @ModelAttribute("student") StudentDTO studentDTO,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Role currentRole = (Role) session.getAttribute("currentRole");
        Long currentUserId = (Long) session.getAttribute("currentUserId");
        
        if (currentRole == Role.STUDENT) {
            // Student can only edit their own profile and cannot change role
            if (!id.equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own profile!");
                return "redirect:/students";
            }
            studentService.updateStudentByStudent(id, studentDTO, currentUserId);
        } else {
            // Teacher can edit any student
            studentService.updateStudent(id, studentDTO, true);
        }
        
        redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        return "redirect:/students";
    }

    // Only Teachers can delete students
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        Role currentRole = (Role) session.getAttribute("currentRole");
        if (currentRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("error", "Only teachers can delete students!");
            return "redirect:/students";
        }
        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        return "redirect:/students";
    }

    @GetMapping("/view/{id}")
    public String viewStudent(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentDTO(id));
        studentService.getStudentById(id).ifPresent(student -> {
            model.addAttribute("studentEntity", student);
        });
        return "student-view";
    }
}

