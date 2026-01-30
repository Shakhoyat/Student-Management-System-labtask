package com.example.webapp.controller;

import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.service.DepartmentService;
import com.example.webapp.service.StudentService;
import com.example.webapp.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping("/store")
    public String storeTeacher(@ModelAttribute("teacher") TeacherDTO teacherDTO) {
        teacherService.saveTeacher(teacherDTO);
        return "redirect:/teachers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("teacher", teacherService.getTeacherDTO(id));
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("students", studentService.getAllStudents());
        return "teacher-form";
    }

    @PostMapping("/update/{id}")
    public String updateTeacher(@PathVariable Long id, @ModelAttribute("teacher") TeacherDTO teacherDTO) {
        teacherService.updateTeacher(id, teacherDTO);
        return "redirect:/teachers";
    }

    @GetMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return "redirect:/teachers";
    }

    @PostMapping("/{teacherId}/assign-student/{studentId}")
    public String assignStudent(@PathVariable Long teacherId, @PathVariable Long studentId) {
        teacherService.assignStudentToTeacher(teacherId, studentId);
        return "redirect:/teachers";
    }
}
