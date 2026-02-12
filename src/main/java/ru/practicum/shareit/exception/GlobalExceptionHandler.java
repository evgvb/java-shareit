package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException ex) {
        log.error("Объект не найден: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Объект не найден");
        error.put("message", ex.getMessage());
        return error;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ValidationException ex) {
        log.error("Ошибка валидации: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка валидации");
        error.put("message", ex.getMessage());
        return error;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Ошибка бизнес-логики: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка бизнес-логики");
        error.put("message", ex.getMessage());
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Ошибка валидации данных: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllExceptions(Exception ex) {
        log.error("Внутренняя ошибка сервера: ", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Внутренняя ошибка сервера");
        error.put("message", ex.getMessage());
        return error;
    }
}