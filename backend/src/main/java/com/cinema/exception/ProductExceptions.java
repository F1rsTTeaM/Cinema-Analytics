package com.cinema.exception;

public final class ProductExceptions {

    private ProductExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Товар с id=" + id + " не найден");
        }
    }

    public static class DuplicateName extends RuntimeException {
        public DuplicateName(String name) {
            super("Товар с названием '" + name + "' уже существует");
        }
    }

    public static class InvalidData extends RuntimeException {
        public InvalidData(String message) {
            super(message);
        }
    }

    public static class InvalidPrice extends InvalidData {
        public InvalidPrice(Object price) {
            super("Некорректная цена товара: " + price
                    + ". Цена должна быть больше 0");
        }
    }

    public static class CannotDelete extends RuntimeException {
        public CannotDelete(Long id, String reason) {
            super("Невозможно удалить товар с id=" + id + ": " + reason);
        }
    }
}