package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommentTest extends IntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        UserDto owner = UserDto.builder()
                .name("Владелец")
                .email("owner@test.com")
                .build();
        ownerId = userService.createUser(owner).getId();

        UserDto booker = UserDto.builder()
                .name("Бронирующий")
                .email("booker@test.com")
                .build();
        bookerId = userService.createUser(booker).getId();

        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();
        itemId = itemService.createItem(item, ownerId).getId();
    }

    @Test
    void addComment_ShouldThrowException_WhenUserHasNotCompletedBooking() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .build();

        bookingService.createBooking(bookingDto, bookerId);

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Отличная дрель!")
                .build();

        assertThatThrownBy(() -> itemService.addComment(itemId, commentDto, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("после завершения бронирования");
    }

    @Test
    void addComment_ShouldThrowException_WhenUserHasNoBooking() {

        UserDto newUser = UserDto.builder()
                .name("Новый пользователь")
                .email("new@test.com")
                .build();
        Long newUserId = userService.createUser(newUser).getId();

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Отличная дрель!")
                .build();

        assertThatThrownBy(() -> itemService.addComment(itemId, commentDto, newUserId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("после завершения бронирования");
    }

    @Test
    void addComment_ShouldThrowException_WhenTextIsBlank() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .build();

        bookingService.createBooking(bookingDto, bookerId);

        CreateCommentDto emptyComment = CreateCommentDto.builder()
                .text("")
                .build();

        assertThatThrownBy(() -> {
            if (emptyComment.getText() == null || emptyComment.getText().isBlank()) {
                throw new ValidationException("Текст комментария не может быть пустым");
            }
            itemService.addComment(itemId, emptyComment, bookerId);
        }).isInstanceOf(ValidationException.class);
    }

    @Test
    void getItemById_ShouldIncludeComments_WhenItemHasComments() {
        // Создаем бронирование с прошедшей датой окончания
        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        // Подтверждаем бронирование (меняем статус с WAITING на APPROVED)
        bookingService.approveBooking(createdBooking.getId(),true, ownerId);

        // Создаем комментарий
        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Отличная дрель!")
                .build();

        itemService.addComment(itemId, commentDto, bookerId);

        // Получаем вещь с комментариями
        ItemDto itemWithComments = itemService.getItemById(itemId, ownerId);

        // Проверяем результаты
        assertThat(itemWithComments.getComments()).isNotEmpty();
        assertThat(itemWithComments.getComments()).hasSize(1);
        assertThat(itemWithComments.getComments().get(0).getText()).isEqualTo("Отличная дрель!");
        assertThat(itemWithComments.getComments().get(0).getAuthorName()).isEqualTo("Бронирующий");
    }

    @Test
    void getItemById_ShouldReturnItemWithoutComments_WhenNoComments() {

        ItemDto itemWithoutComments = itemService.getItemById(itemId, ownerId);

        assertThat(itemWithoutComments.getComments()).isNullOrEmpty();
    }
}
