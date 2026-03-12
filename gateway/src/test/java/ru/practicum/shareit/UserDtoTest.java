package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValidForCreate_thenNoViolations() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto, UserDto.Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenNameBlankForCreate_thenViolations() {
        UserDto userDto = UserDto.builder()
                .name("")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto, UserDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Имя не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailBlankForCreate_thenViolations() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto, UserDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailInvalidForCreate_thenViolations() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto, UserDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Некорректный формат email", violations.iterator().next().getMessage());
    }

    @Test
    void whenUpdateWithEmptyFields_thenNoViolations() {
        UserDto userDto = UserDto.builder().build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto, UserDto.Update.class);
        assertTrue(violations.isEmpty());
    }
}