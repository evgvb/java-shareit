package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValidForCreate_thenNoViolations() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto, ItemDto.Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenNameBlankForCreate_thenViolations() {
        ItemDto itemDto = ItemDto.builder()
                .name("")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto, ItemDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenDescriptionBlankForCreate_thenViolations() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto, ItemDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Описание не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenAvailableNullForCreate_thenViolations() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto, ItemDto.Create.class);
        assertFalse(violations.isEmpty());
        assertEquals("Статус доступности должен быть указан", violations.iterator().next().getMessage());
    }

    @Test
    void whenUpdateWithEmptyFields_thenNoViolations() {
        ItemDto itemDto = ItemDto.builder().build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto, ItemDto.Update.class);
        assertTrue(violations.isEmpty());
    }
}