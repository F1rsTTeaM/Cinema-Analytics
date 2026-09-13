package com.cinema.exception;

public final class HallExceptions {

    private HallExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Зал с id=" + id + " не найден");
        }
    }

    public static class DuplicateName extends RuntimeException {
        public DuplicateName(String name) {
            super("Зал с названием '" + name + "' уже существует");
        }
    }

    public static class InvalidData extends RuntimeException {
        public InvalidData(String message) {
            super(message);
        }
    }

    public static class InvalidRowsCount extends InvalidData {
        public InvalidRowsCount(Integer rows) {
            super("Некорректное количество рядов: " + rows
                    + ". Должно быть больше 0");
        }
    }

    public static class InvalidSeatsPerRow extends InvalidData {
        public InvalidSeatsPerRow(Integer seats) {
            super("Некорректное количество мест в ряду: " + seats
                    + ". Должно быть больше 0");
        }
    }

    public static class CannotDelete extends RuntimeException {
        public CannotDelete(Long id, String reason) {
            super("Невозможно удалить зал с id=" + id + ": " + reason);
        }
    }
}