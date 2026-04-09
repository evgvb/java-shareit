package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void userDto_ShouldFailValidation_WhenNameIsBlank() {
        UserDto dto = UserDto.builder().name("").email("user@email.com").build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto, UserDto.Create.class);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void userDto_ShouldFailValidation_WhenEmailIsInvalid() {
        UserDto dto = UserDto.builder().name("user").email("плохой емаил").build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto, UserDto.Create.class);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void itemDto_ShouldFailValidation_WhenNameIsBlank() {
        ItemDto dto = ItemDto.builder().name("").description("пустое имя").available(true).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto, ItemDto.Create.class);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bookingDto_ShouldFailValidation_WhenEndDateIsInPast() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1)) // В прошлом
                .itemId(1L)
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void createCommentDto_ShouldFailValidation_WhenTextIsBlank() {
        CreateCommentDto dto = CreateCommentDto.builder().text("").build();
        Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void createItemRequestDto_ShouldFailValidation_WhenDescriptionIsBlank() {
        CreateItemRequestDto dto = CreateItemRequestDto.builder().description("").build();
        Set<ConstraintViolation<CreateItemRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
}