package com.example.webapp.controller;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final DepartmentService departmentService;

    public CourseController(CourseService courseService, DepartmentService departmentService) {
        this.courseService = courseService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String getAllCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "courses";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("course", new CourseDTO());
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "course-form";
    }

    @PostMapping("/store")
    public String storeCourse(@ModelAttribute("course") CourseDTO courseDTO) {
        courseService.saveCourse(courseDTO);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getCourseDTO(id));
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "course-form";
    }

    @PostMapping("/update/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute("course") CourseDTO courseDTO) {
        courseService.updateCourse(id, courseDTO);
        return "redirect:/courses";
    }

    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }
}
