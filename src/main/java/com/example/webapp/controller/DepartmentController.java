package com.example.webapp.controller;

import com.example.webapp.dto.DepartmentDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.DepartmentService;
import com.example.webapp.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final TeacherService teacherService;
    private final CourseService courseService;

    public DepartmentController(DepartmentService departmentService,
                               TeacherService teacherService,
                               CourseService courseService) {
        this.departmentService = departmentService;
        this.teacherService = teacherService;
        this.courseService = courseService;
    }

    @GetMapping
    public String getAllDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        return "departments";
    }

    @GetMapping("/{id}")
    public String viewDepartment(@PathVariable Long id, Model model) {
        DepartmentDTO department = departmentService.getDepartmentDTO(id);
        model.addAttribute("department", department);
        if (department.getTeacherIds() != null && !department.getTeacherIds().isEmpty()) {
            model.addAttribute("teachers", teacherService.getTeachersByIds(department.getTeacherIds()));
        }
        if (department.getCourseIds() != null && !department.getCourseIds().isEmpty()) {
            model.addAttribute("courses", courseService.getCoursesByIds(department.getCourseIds()));
        }
        return "department-view";
    }

    @GetMapping("/new")
    public String showAddForm(Model model, HttpSession session) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            return "redirect:/departments";
        }
        model.addAttribute("department", new DepartmentDTO());
        return "department-form";
    }

    @PostMapping
    public String storeDepartment(@ModelAttribute("department") DepartmentDTO departmentDTO,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can create departments");
            return "redirect:/departments";
        }
        departmentService.saveDepartment(departmentDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Department created successfully");
        return "redirect:/departments";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            return "redirect:/departments";
        }
        model.addAttribute("department", departmentService.getDepartmentDTO(id));
        return "department-form";
    }

    @PostMapping("/{id}/edit")
    public String updateDepartment(@PathVariable Long id, 
                                  @ModelAttribute("department") DepartmentDTO departmentDTO,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can update departments");
            return "redirect:/departments";
        }
        departmentService.updateDepartment(id, departmentDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully");
        return "redirect:/departments";
    }

    @PostMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Role userRole = (Role) session.getAttribute("userRole");
        if (userRole != Role.TEACHER) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only teachers can delete departments");
            return "redirect:/departments";
        }
        departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully");
        return "redirect:/departments";
    }
}
