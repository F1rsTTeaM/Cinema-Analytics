package com.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.config.JwtService;
import com.cinema.dto.AuthRequest;
import com.cinema.dto.AuthResponse;
import com.cinema.dto.RegisterRequest;
import com.cinema.exception.UserExceptions;
import com.cinema.model.User;
import com.cinema.service.UserService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j 
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - регистрация пользователя: '{}'", request.getUsername());

        User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        log.info("POST /api/auth/register - пользователь '{}' зарегистрирован", user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        log.info("POST /api/auth/login - попытка входа: '{}'", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("POST /api/auth/login - неверные учётные данные для '{}'", request.getUsername());
            throw new UserExceptions.InvalidCredentials();
        }

        User user = userService.findByUsername(request.getUsername());
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        log.info("POST /api/auth/login - пользователь '{}' успешно вошёл", user.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }
}
