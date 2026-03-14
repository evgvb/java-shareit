package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenStartNull_thenViolations() {
        BookingDto bookingDto = BookingDto.builder()
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата начала не может быть null", violations.iterator().next().getMessage());
    }

    @Test
    void whenEndNull_thenViolations() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата окончания не может быть null", violations.iterator().next().getMessage());
    }

    @Test
    void whenItemIdNull_thenViolations() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("ID вещи не может быть null", violations.iterator().next().getMessage());
    }

//    @Test
//    void whenStartInPast_thenViolations() {
//        BookingDto bookingDto = BookingDto.builder()
//                .start(LocalDateTime.now().minusDays(1))
//                .end(LocalDateTime.now().plusDays(2))
//                .itemId(1L)
//                .build();
//
//        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
//        assertFalse(violations.isEmpty());
//        assertEquals("Дата начала должна быть в настоящем или будущем",
//                violations.iterator().next().getMessage());
//    }

    @Test
    void whenEndInPast_thenViolations() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
        assertEquals("Дата окончания должна быть в будущем",
                violations.iterator().next().getMessage());
    }
}