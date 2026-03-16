package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void handleValidationException_WithMethodArgumentNotValidException_ShouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("objectName", "field", "Field error message");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> result = errorHandler.handleValidationException(ex);

        assertThat(result)
                .containsEntry("error", "Ошибка валидации")
                .containsKey("message");
    }

    @Test
    void handleValidationException_WithValidationException_ShouldReturn400() {
        ValidationException ex = new ValidationException("Validation error");

        Map<String, String> result = errorHandler.handleValidationException(ex);

        assertThat(result)
                .containsEntry("error", "Ошибка валидации")
                .containsEntry("message", "Validation error");
    }

    @Test
    void handleValidationException_WithConstraintViolationException_ShouldReturn400() {
        Exception ex = new Exception("Constraint violation");

        Map<String, String> result = errorHandler.handleValidationException(ex);

        assertThat(result)
                .containsEntry("error", "Ошибка валидации")
                .containsEntry("message", "Constraint violation");
    }

    @Test
    void handleNotFoundException_ShouldReturn404() {
        NoSuchElementException ex = new NoSuchElementException("Not found");

        Map<String, String> result = errorHandler.handleNotFoundException(ex);

        assertThat(result)
                .containsEntry("error", "Объект не найден")
                .containsEntry("message", "Not found");
    }

    @Test
    void handleIllegalArgument_ShouldReturn409() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");

        Map<String, String> result = errorHandler.handleIllegalArgument(ex);

        assertThat(result)
                .containsEntry("error", "Ошибка бизнес-логики")
                .containsEntry("message", "Illegal argument");
    }

    @Test
    void handleException_ShouldReturn500() {
        Exception ex = new RuntimeException("Internal error");

        Map<String, String> result = errorHandler.handleException(ex);

        assertThat(result)
                .containsEntry("error", "Внутренняя ошибка сервера")
                .containsEntry("message", "Internal error");
    }

    @Test
    void handleException_WithNullMessage_ShouldHandleGracefully() {
        Exception ex = new NullPointerException();

        Map<String, String> result = errorHandler.handleException(ex);

        assertThat(result)
                .containsEntry("error", "Внутренняя ошибка сервера")
                .containsEntry("message", (String) null);
    }
}