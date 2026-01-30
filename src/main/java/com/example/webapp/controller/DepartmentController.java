package com.example.webapp.controller;

import com.example.webapp.dto.DepartmentDTO;
import com.example.webapp.service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String getAllDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        return "departments";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("department", new DepartmentDTO());
        return "department-form";
    }

    @PostMapping("/store")
    public String storeDepartment(@ModelAttribute("department") DepartmentDTO departmentDTO) {
        departmentService.saveDepartment(departmentDTO);
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        departmentService.getDepartmentById(id).ifPresent(department -> {
            DepartmentDTO dto = new DepartmentDTO(department.getId(), department.getName());
            model.addAttribute("department", dto);
        });
        return "department-form";
    }

    @PostMapping("/update/{id}")
    public String updateDepartment(@PathVariable Long id, @ModelAttribute("department") DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(id, departmentDTO);
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "redirect:/departments";
    }
}
