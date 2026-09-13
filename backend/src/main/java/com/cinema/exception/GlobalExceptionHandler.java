package com.cinema.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @PostConstruct
    public void init() {
        log.info("=== GlobalExceptionHandler загружен, пакет: {} ===",
                getClass().getPackageName());
    }

    // ---------- Movie ----------

    @ExceptionHandler(MovieExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleMovieNotFound(MovieExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MovieExceptions.NotFoundByTitle.class)
    public ResponseEntity<ErrorResponse> handleMovieNotFoundByTitle(MovieExceptions.NotFoundByTitle ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MovieExceptions.DuplicateTitle.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTitle(MovieExceptions.DuplicateTitle ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MovieExceptions.InvalidData.class)
    public ResponseEntity<ErrorResponse> handleMovieInvalidData(MovieExceptions.InvalidData ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MovieExceptions.CannotDelete.class)
    public ResponseEntity<ErrorResponse> handleCannotDelete(MovieExceptions.CannotDelete ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ---------- Hall ----------

    @ExceptionHandler(HallExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleHallNotFound(HallExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(HallExceptions.DuplicateName.class)
    public ResponseEntity<ErrorResponse> handleHallDuplicateName(HallExceptions.DuplicateName ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(HallExceptions.InvalidData.class)
    public ResponseEntity<ErrorResponse> handleHallInvalidData(HallExceptions.InvalidData ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(HallExceptions.CannotDelete.class)
    public ResponseEntity<ErrorResponse> handleHallCannotDelete(HallExceptions.CannotDelete ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ---------- Product ----------

    @ExceptionHandler(ProductExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductExceptions.DuplicateName.class)
    public ResponseEntity<ErrorResponse> handleProductDuplicateName(ProductExceptions.DuplicateName ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductExceptions.InvalidData.class)
    public ResponseEntity<ErrorResponse> handleProductInvalidData(ProductExceptions.InvalidData ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductExceptions.CannotDelete.class)
    public ResponseEntity<ErrorResponse> handleProductCannotDelete(ProductExceptions.CannotDelete ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ---------- ProductSale ----------

    @ExceptionHandler(ProductSaleExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleSaleNotFound(ProductSaleExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductSaleExceptions.InvalidData.class)
    public ResponseEntity<ErrorResponse> handleSaleInvalidData(ProductSaleExceptions.InvalidData ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductSaleExceptions.ProductNotFound.class)
    public ResponseEntity<ErrorResponse> handleSaleProductNotFound(ProductSaleExceptions.ProductNotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ---------- User ----------

    @ExceptionHandler(UserExceptions.DuplicateUsername.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(UserExceptions.DuplicateUsername ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserExceptions.DuplicateEmail.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(UserExceptions.DuplicateEmail ex) {
        log.warn("409: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserExceptions.InvalidCredentials.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(UserExceptions.InvalidCredentials ex) {
        log.warn("401: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserExceptions.InvalidRole.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(UserExceptions.InvalidRole ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ---------- Report ----------

    @ExceptionHandler(ReportExceptions.UnknownReportType.class)
    public ResponseEntity<ErrorResponse> handleUnknownReportType(ReportExceptions.UnknownReportType ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReportExceptions.UnknownFormat.class)
    public ResponseEntity<ErrorResponse> handleUnknownFormat(ReportExceptions.UnknownFormat ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReportExceptions.InvalidPeriod.class)
    public ResponseEntity<ErrorResponse> handleInvalidPeriod(ReportExceptions.InvalidPeriod ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReportExceptions.EmailSendFailed.class)
    public ResponseEntity<ErrorResponse> handleEmailSendFailed(ReportExceptions.EmailSendFailed ex) {
        log.error("500: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Не удалось отправить отчёт: " + ex.getMessage()));
    }

    // ---------- Validation ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        log.warn("400: ошибки валидации: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    // ---------- Fallback ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        log.error("Непредвиденная ошибка", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Внутренняя ошибка сервера"));
    }
}