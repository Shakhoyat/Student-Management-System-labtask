package com.example.webapp.controller;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import com.example.webapp.entity.Teacher;
import com.example.webapp.repository.StudentRepository;
import com.example.webapp.repository.TeacherRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AuthController(TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        model.addAttribute("currentRole", session.getAttribute("currentRole"));
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("students", studentRepository.findAll());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String role, 
                        @RequestParam Long userId,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        if ("TEACHER".equals(role)) {
            Optional<Teacher> teacher = teacherRepository.findById(userId);
            if (teacher.isPresent()) {
                session.setAttribute("currentUserId", teacher.get().getId());
                session.setAttribute("currentUser", teacher.get().getName());
                session.setAttribute("currentRole", Role.TEACHER);
                session.setAttribute("currentEmail", teacher.get().getEmail());
                redirectAttributes.addFlashAttribute("success", "Welcome, " + teacher.get().getName() + "!");
                return "redirect:/";
            }
        } else if ("STUDENT".equals(role)) {
            Optional<Student> student = studentRepository.findById(userId);
            if (student.isPresent()) {
                session.setAttribute("currentUserId", student.get().getId());
                session.setAttribute("currentUser", student.get().getName());
                session.setAttribute("currentRole", Role.STUDENT);
                session.setAttribute("currentEmail", student.get().getEmail());
                redirectAttributes.addFlashAttribute("success", "Welcome, " + student.get().getName() + "!");
                return "redirect:/";
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "Invalid login!");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Logged out successfully!");
        return "redirect:/login";
    }

    // Quick login as demo teacher (for testing)
    @GetMapping("/login/demo-teacher")
    public String demoTeacherLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        // Create a demo teacher session
        session.setAttribute("currentUserId", 0L);
        session.setAttribute("currentUser", "Demo Teacher");
        session.setAttribute("currentRole", Role.TEACHER);
        session.setAttribute("currentEmail", "teacher@demo.com");
        redirectAttributes.addFlashAttribute("success", "Logged in as Demo Teacher!");
        return "redirect:/";
    }

    // Quick login as demo student (for testing)  
    @GetMapping("/login/demo-student")
    public String demoStudentLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("currentUserId", 0L);
        session.setAttribute("currentUser", "Demo Student");
        session.setAttribute("currentRole", Role.STUDENT);
        session.setAttribute("currentEmail", "student@demo.com");
        redirectAttributes.addFlashAttribute("success", "Logged in as Demo Student!");
        return "redirect:/";
    }
}
