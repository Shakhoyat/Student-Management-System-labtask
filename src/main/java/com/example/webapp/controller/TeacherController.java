package com.example.webapp.controller;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.service.DepartmentService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final DepartmentService departmentService;
    private final StudentService studentService;

    public TeacherController(TeacherService teacherService, 
                            DepartmentService departmentService,
                            StudentService studentService) {
        this.teacherService = teacherService;
        this.departmentService = departmentService;
        this.studentService = studentService;
    }

    @GetMapping
    public String getAllTeachers(Model model) {
        model.addAttribute("teachers", teacherService.getAllTeachersDTO());
        return "teachers";
    }

    @GetMapping("/{id}")
    public String viewTeacher(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getTeacherDTO(id);
        model.addAttribute("teacher", teacher);
        if (teacher.getStudentIds() != null && !teacher.getStudentIds().isEmpty()) {
            model.addAttribute("students", studentService.getStudentsByIds(teacher.getStudentIds()));
        }
        return "teacher-view";
    }

    @GetMapping("/new")
    public String showAddForm(Model model, HttpSession session) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            return "redirect:/teachers";
        }
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping
    public String storeTeacher(@ModelAttribute("teacher") TeacherDTO teacherDTO,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can create teacher profiles");
            return "redirect:/teachers";
        }
        teacherService.saveTeacher(teacherDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher created successfully");
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            return "redirect:/teachers";
        }
        model.addAttribute("teacher", teacherService.getTeacherDTO(id));
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping("/{id}/edit")
    public String updateTeacher(@PathVariable Long id, 
                               @ModelAttribute("teacher") TeacherDTO teacherDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can update teacher profiles");
            return "redirect:/teachers";
        }
        teacherService.updateTeacher(id, teacherDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully");
        return "redirect:/teachers";
    }

    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can delete teacher profiles");
            return "redirect:/teachers";
        }
        teacherService.deleteTeacher(id);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully");
        return "redirect:/teachers";
    }
}
