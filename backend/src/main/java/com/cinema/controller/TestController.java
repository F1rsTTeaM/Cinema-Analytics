package com.cinema.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Backend работает";
    }
    
    @GetMapping("/api/user/profile1")
    public String profile() {
        return "Это защищённый эндпоинт. Ты авторизован!";
    }
}
