package com.cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.exception.UserExceptions;
import com.cinema.model.Role;
import com.cinema.model.User;
import com.cinema.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional 
@Slf4j 
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password, String role) {
        log.debug("Регистрация пользователя: username='{}', email='{}', role='{}'",
                username, email, role);

        if (userRepository.existsByUsername(username)) {
            log.warn("Попытка регистрации с уже существующим username: '{}'", username);
            throw new UserExceptions.DuplicateUsername(username);
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Попытка регистрации с уже используемым email: '{}'", email);
            throw new UserExceptions.DuplicateEmail(email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        User saved = userRepository.save(user);
        log.info("Пользователь '{}' успешно зарегистрирован с id={} и ролью {}",
                saved.getUsername(), saved.getId(), saved.getRole());
        
                return saved;
    }

    public User findByUsername(String username) {
        log.debug("Поиск пользователя по username='{}'", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь '{}' не найден", username);
                    return new UserExceptions.NotFound(username);
                });
    }
}
