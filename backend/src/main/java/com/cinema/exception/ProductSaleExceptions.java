package com.cinema.exception;

public final class ProductSaleExceptions {

    private ProductSaleExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Продажа с id=" + id + " не найдена");
        }
    }

    public static class InvalidData extends RuntimeException {
        public InvalidData(String message) {
            super(message);
        }
    }

    public static class InvalidCount extends InvalidData {
        public InvalidCount(Integer count) {
            super("Некорректное количество товара: " + count
                    + ". Количество должно быть больше 0");
        }
    }

    public static class ProductNotFound extends RuntimeException {
        public ProductNotFound(Long productId) {
            super("Невозможно создать продажу: товар с id=" + productId + " не найден");
        }
    }
}