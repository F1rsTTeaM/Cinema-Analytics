package com.cinema.exception;

public final class MovieExceptions {

    private MovieExceptions() {
    }

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Фильм с id=" + id + " не найден");
        }
    }

    public static class NotFoundByTitle extends RuntimeException {
        public NotFoundByTitle(String title) {
            super("Фильм с названием '" + title + "' не найден");
        }
    }

    public static class DuplicateTitle extends RuntimeException {
        public DuplicateTitle(String title) {
            super("Фильм с названием '" + title + "' уже существует");
        }
    }

    public static class InvalidData extends RuntimeException {
        public InvalidData(String message) {
            super(message);
        }
    }

    public static class InvalidDuration extends InvalidData {
        public InvalidDuration(Integer duration) {
            super("Некорректная длительность фильма: " + duration
                    + " мин. Длительность должна быть больше 0");
        }
    }

    public static class CannotDelete extends RuntimeException {
        public CannotDelete(Long id, String reason) {
            super("Невозможно удалить фильм с id=" + id + ": " + reason);
        }
    }
}