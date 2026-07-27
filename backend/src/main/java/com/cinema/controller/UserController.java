package com.cinema.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/dashboard")
    public Map<String, Object> userDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().toString();

        Map<String, Object> response = new HashMap<>();
        response.put("page", "Страница пользователя");
        response.put("username", username);
        response.put("role", role);
        response.put("message", "Эта страница доступна всем авторизованным пользователям");
        return response;
    }

    @GetMapping("/profile")
    public Map<String, String> userProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> response = new HashMap<>();
        response.put("page", "Профиль пользователя");
        response.put("username", auth.getName());
        response.put("message", "Ваш профиль");
        return response;
    }
}