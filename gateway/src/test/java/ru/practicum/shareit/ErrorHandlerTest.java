package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.practicum.shareit.exception.ErrorHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleValidationExceptions_whenCalled_thenReturnBadRequest() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleValidationExceptions(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("field: error message", response.getBody().get("error"));
    }

    @Test
    void handleValidationExceptions_whenMultipleErrors_thenReturnCombinedMessage() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "name", "Name is required");
        FieldError fieldError2 = new FieldError("object", "email", "Email is invalid");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleValidationExceptions(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("name: Name is required, email: Email is invalid", response.getBody().get("error"));
    }

    @Test
    void handleConstraintViolationException_whenCalled_thenReturnBadRequest() {
        // Given
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("createUser.userDto.email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Email не может быть пустым");
        when(ex.getConstraintViolations()).thenReturn(Set.of(violation));

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleConstraintViolationException(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("createUser.userDto.email: Email не может быть пустым", response.getBody().get("error"));
    }

    @Test
    void handleHandlerMethodValidationException_whenCalled_thenReturnBadRequest() {
        // Given
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);

        // Создаем мок для HandlerMethodValidationException сложнее,
        // но для простоты теста можно проверить только статус код
        doReturn(List.of()).when(ex).getAllValidationResults();

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleHandlerMethodValidationException(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void handleMissingRequestHeaderException_whenCalled_thenReturnBadRequest() {
        // Given
        MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
        when(ex.getHeaderName()).thenReturn("X-Sharer-User-Id");

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleMissingRequestHeaderException(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Отсутствует обязательный заголовок: X-Sharer-User-Id", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_whenCalled_thenReturnBadRequest() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleIllegalArgument(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Invalid argument", response.getBody().get("error"));
    }

    @Test
    void handleException_whenCalled_thenReturnInternalServerError() {
        // Given
        Exception ex = new Exception("Internal error");

        // When
        ResponseEntity<Map<String, String>> response = errorHandler.handleException(ex);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Произошла внутренняя ошибка сервера", response.getBody().get("error"));
    }
}