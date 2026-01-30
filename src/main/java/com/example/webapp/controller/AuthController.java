package com.example.webapp.controller;

import com.example.webapp.entity.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        return "home";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/auth/login")
    public String login(@RequestParam String name,
                        @RequestParam String role,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Role userRole = Role.valueOf(role);
            session.setAttribute("userId", 0L);
            session.setAttribute("userName", name);
            session.setAttribute("userRole", userRole);
            redirectAttributes.addFlashAttribute("successMessage", "Welcome, " + name + "!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid role!");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping("/auth/demo-teacher")
    public String demoTeacherLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("userId", 0L);
        session.setAttribute("userName", "Demo Teacher");
        session.setAttribute("userRole", Role.TEACHER);
        redirectAttributes.addFlashAttribute("successMessage", "Logged in as Demo Teacher!");
        return "redirect:/";
    }

    @GetMapping("/auth/demo-student")
    public String demoStudentLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("userId", 0L);
        session.setAttribute("userName", "Demo Student");
        session.setAttribute("userRole", Role.STUDENT);
        redirectAttributes.addFlashAttribute("successMessage", "Logged in as Demo Student!");
        return "redirect:/";
    }
}
