package com.cinema.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public Map<String, Object> adminDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("page", "Админ-панель");
        response.put("username", username);
        response.put("role", "ADMIN");
        response.put("message", "Эта страница доступна только администраторам");
        response.put("secret", "Секретная информация для администраторов");
        return response;
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("page", "Управление пользователями");
        response.put("users", new String[]{"admin", "manager", "user1", "user2"});
        response.put("message", "Только для администраторов");
        return response;
    }
}