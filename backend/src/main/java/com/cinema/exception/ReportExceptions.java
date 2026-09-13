package com.cinema.exception;

public final class ReportExceptions {

    private ReportExceptions() {}

    public static class UnknownReportType extends RuntimeException {
        public UnknownReportType(String reportType) {
            super("Неизвестный тип отчёта: '" + reportType + "'. "
                    + "Допустимые: tickets-summary, tickets-movies, tickets-halls, "
                    + "tickets-daily, products-summary, products-list, products-daily");
        }
    }

    public static class UnknownFormat extends RuntimeException {
        public UnknownFormat(String format) {
            super("Неизвестный формат экспорта: '" + format + "'. "
                    + "Допустимые: csv, json, pdf");
        }
    }

    public static class InvalidPeriod extends RuntimeException {
        public InvalidPeriod(String message) {
            super(message);
        }
    }

    public static class PeriodTooLong extends InvalidPeriod {
        public PeriodTooLong(long days, long maxDays) {
            super("Период отчёта слишком большой: " + days
                    + " дней. Максимум: " + maxDays + " дней");
        }
    }

    public static class EmailSendFailed extends RuntimeException {
        public EmailSendFailed(String toEmail, Throwable cause) {
            super("Не удалось отправить отчёт на '" + toEmail + "': " + cause.getMessage(), cause);
        }
    }
}