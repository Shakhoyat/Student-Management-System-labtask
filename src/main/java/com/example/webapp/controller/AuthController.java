package com.example.webapp.controller;

import com.example.webapp.dto.RegisterDTO;
import com.example.webapp.entity.Role;
import com.example.webapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
        }
        return "home";
    }

    @GetMapping("/auth/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           @RequestParam(value = "expired", required = false) String expired,
                           Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        if (expired != null) {
            model.addAttribute("errorMessage", "Your session has expired. Please login again");
        }
        return "login";
    }

    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "register";
        }

        // Check if passwords match
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match");
            model.addAttribute("roles", Role.values());
            return "register";
        }

        // Check if username exists
        if (userService.existsByUsername(registerDTO.getUsername())) {
            model.addAttribute("errorMessage", "Username already exists");
            model.addAttribute("roles", Role.values());
            return "register";
        }

        try {
            userService.registerUser(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please login with your credentials.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
