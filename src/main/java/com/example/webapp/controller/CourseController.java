package com.example.webapp.controller;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.service.CourseService;
import com.example.webapp.service.DepartmentService;
import com.example.webapp.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final DepartmentService departmentService;
    private final StudentService studentService;

    public CourseController(CourseService courseService, 
                           DepartmentService departmentService,
                           StudentService studentService) {
        this.courseService = courseService;
        this.departmentService = departmentService;
        this.studentService = studentService;
    }

    @GetMapping
    public String getAllCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCoursesDTO());
        return "courses";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        CourseDTO course = courseService.getCourseDTO(id);
        model.addAttribute("course", course);
        if (course.getStudentIds() != null && !course.getStudentIds().isEmpty()) {
            model.addAttribute("students", studentService.getStudentsByIds(course.getStudentIds()));
        }
        return "course-view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('TEACHER')")
    public String showAddForm(Model model) {
        model.addAttribute("course", new CourseDTO());
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "course-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public String storeCourse(@ModelAttribute("course") CourseDTO courseDTO,
                             RedirectAttributes redirectAttributes) {
        courseService.saveCourse(courseDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Course created successfully");
        return "redirect:/courses";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getCourseDTO(id));
        model.addAttribute("departments", departmentService.getAllDepartmentsDTO());
        model.addAttribute("students", studentService.getAllStudents());
        return "course-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('TEACHER')")
    public String updateCourse(@PathVariable Long id, 
                              @ModelAttribute("course") CourseDTO courseDTO,
                              RedirectAttributes redirectAttributes) {
        courseService.updateCourse(id, courseDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully");
        return "redirect:/courses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteCourse(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully");
        return "redirect:/courses";
    }
}
