package com.example.webapp.controller;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.service.DepartmentService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('TEACHER')")
    public String showAddForm(Model model) {
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public String storeTeacher(@ModelAttribute("teacher") TeacherDTO teacherDTO,
                              RedirectAttributes redirectAttributes) {
        teacherService.saveTeacher(teacherDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher created successfully");
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("teacher", teacherService.getTeacherDTO(id));
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String updateTeacher(@PathVariable Long id, 
                               @ModelAttribute("teacher") TeacherDTO teacherDTO,
                               RedirectAttributes redirectAttributes) {
        teacherService.updateTeacher(id, teacherDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully");
        return "redirect:/teachers";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteTeacher(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        teacherService.deleteTeacher(id);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully");
        return "redirect:/teachers";
    }
}
