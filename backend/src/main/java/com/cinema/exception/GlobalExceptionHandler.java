package com.cinema.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(MovieExceptions.NotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFound(MovieExceptions.NotFound ex) {
        log.warn("404: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MovieExceptions.NotFoundByTitle.class)
    public ResponseEntity<ErrorResponse> handleNotFoundByTitle(MovieExceptions.NotFoundByTitle ex) {
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
    public ResponseEntity<ErrorResponse> handleInvalidData(MovieExceptions.InvalidData ex) {
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        log.error("Непредвиденная ошибка", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Внутренняя ошибка сервера"));
    }
}