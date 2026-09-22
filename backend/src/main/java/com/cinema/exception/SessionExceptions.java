package com.cinema.exception;

public final class SessionExceptions {

    private SessionExceptions() {
    }

    public static class NotFound extends RuntimeException {
        public NotFound(Long id) {
            super("Сеанс с id=" + id + " не найден");
        }
    }

    public static class InvalidData extends RuntimeException {
        public InvalidData(String message) {
            super(message);
        }
    }

    public static class InvalidTimeRange extends InvalidData {
        public InvalidTimeRange() {
            super("Время окончания должно быть позже времени начала");
        }
    }

    public static class DurationTooShort extends InvalidData {
        public DurationTooShort(long minutes, long minMinutes) {
            super("Длительность сеанса " + minutes
                    + " мин. слишком мала. Минимум: " + minMinutes + " мин.");
        }
    }

    public static class TimeOverlap extends RuntimeException {
        public TimeOverlap(Long hallId) {
            super("В этом зале уже есть сеанс в указанное время");
        }
    }

    public static class InvalidStatus extends RuntimeException {
        public InvalidStatus(String status) {
            super("Неверный статус сеанса: '" + status + "'. "
                    + "Допустимые: SCHEDULED, COMPLETED, CANCELLED, SOLD_OUT");
        }
    }

    public static class Cancelled extends RuntimeException {
        public Cancelled(Long id) {
            super("Сеанс с id=" + id + " отменён. Покупка билетов недоступна");
        }
    }

    public static class Completed extends RuntimeException {
        public Completed(Long id) {
            super("Сеанс с id=" + id + " уже завершён. Покупка билетов недоступна");
        }
    }

    public static class SeatOccupied extends RuntimeException {
        public SeatOccupied(String seat) {
            super("Место '" + seat + "' уже занято");
        }
    }

    public static class InvalidSeat extends InvalidData {
        public InvalidSeat(String seat) {
            super("Некорректное место: '" + seat + "'");
        }
    }

    public static class NoSeatsSelected extends InvalidData {
        public NoSeatsSelected() {
            super("Не выбрано ни одного места для покупки");
        }
    }

    public static class CannotDelete extends RuntimeException {
        public CannotDelete(Long id, String reason) {
            super("Невозможно удалить сеанс с id=" + id + ": " + reason);
        }
    }

    public static class InvalidPeriod extends RuntimeException {
        public InvalidPeriod(String message) {
            super(message);
        }
    }
}