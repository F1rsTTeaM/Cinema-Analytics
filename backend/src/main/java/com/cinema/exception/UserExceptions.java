package com.cinema.exception;

public final class UserExceptions {

    private UserExceptions() {}

    public static class DuplicateUsername extends RuntimeException {
        public DuplicateUsername(String username) {
            super("Пользователь с именем '" + username + "' уже существует");
        }
    }

    public static class DuplicateEmail extends RuntimeException {
        public DuplicateEmail(String email) {
            super("Email '" + email + "' уже используется");
        }
    }

    public static class NotFound extends RuntimeException {
        public NotFound(String username) {
            super("Пользователь '" + username + "' не найден");
        }
    }

    public static class InvalidCredentials extends RuntimeException {
        public InvalidCredentials() {
            super("Неверный логин или пароль");
        }
    }

    public static class InvalidRole extends RuntimeException {
        public InvalidRole(String role) {
            super("Некорректная роль: '" + role + "'. Допустимые значения: USER, ADMIN");
        }
    }
}