package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookingTest extends IntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

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

        UserDto savedOwner = userService.createUser(owner);
        ownerId = savedOwner.getId();

        UserDto booker = UserDto.builder()
                .name("Бронирующий")
                .email("booker@test.com")
                .build();

        UserDto savedBooker = userService.createUser(booker);
        bookerId = savedBooker.getId();

        ItemDto item = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(item, ownerId);
        itemId = savedItem.getId();
    }

    @Test
    void createBooking_ShouldCreateBooking_WhenDataIsValid() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        assertThat(createdBooking.getId()).isNotNull();
        assertThat(createdBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(createdBooking.getBooker().getId()).isEqualTo(bookerId);
        assertThat(createdBooking.getItem().getId()).isEqualTo(itemId);
        assertThat(createdBooking.getStart()).isEqualTo(bookingDto.getStart());
        assertThat(createdBooking.getEnd()).isEqualTo(bookingDto.getEnd());

        assertThat(bookingRepository.findById(createdBooking.getId())).isPresent();
    }

    @Test
    void createBooking_ShouldThrowException_WhenItemNotAvailable() {

        Map<String, Object> updates = Map.of("available", false);
        itemService.updateItem(itemId, updates, ownerId);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна");
    }

    @Test
    void createBooking_ShouldThrowException_WhenEndDateBeforeStartDate() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(3))
                .end(now.plusDays(1))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, bookerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата окончания должна быть больше даты начала");
    }

    @Test
    void createBooking_ShouldThrowException_WhenOwnerTriesToBookOwnItem() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, ownerId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Владелец не может забронировать свою вещь");
    }

    @Test
    void approveBooking_ShouldApproveBooking_WhenOwnerApproves() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto approvedBooking = bookingService.approveBooking(
                createdBooking.getId(), true, ownerId);

        assertThat(approvedBooking.getId()).isEqualTo(createdBooking.getId());
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_ShouldRejectBooking_WhenOwnerRejects() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto rejectedBooking = bookingService.approveBooking(
                createdBooking.getId(), false, ownerId);

        assertThat(rejectedBooking.getId()).isEqualTo(createdBooking.getId());
        assertThat(rejectedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_ShouldThrowException_WhenUserIsNotOwner() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        UserDto stranger = UserDto.builder()
                .name("Чужой")
                .email("stranger@test.com")
                .build();

        UserDto savedStranger = userService.createUser(stranger);

        assertThatThrownBy(() -> bookingService.approveBooking(
                createdBooking.getId(), true, savedStranger.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("не является владельцем");
    }

    @Test
    void approveBooking_ShouldThrowException_WhenBookingAlreadyApproved() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);
        bookingService.approveBooking(createdBooking.getId(), true, ownerId);

        assertThatThrownBy(() -> bookingService.approveBooking(
                createdBooking.getId(), true, ownerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("уже обработано");
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserIsBooker() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto foundBooking = bookingService.getBookingById(
                createdBooking.getId(), bookerId);

        assertThat(foundBooking.getId()).isEqualTo(createdBooking.getId());
        assertThat(foundBooking.getBooker().getId()).isEqualTo(bookerId);
        assertThat(foundBooking.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserIsOwner() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto foundBooking = bookingService.getBookingById(
                createdBooking.getId(), ownerId);

        assertThat(foundBooking.getId()).isEqualTo(createdBooking.getId());
    }

    @Test
    void getBookingById_ShouldThrowException_WhenUserIsNotAuthorized() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, bookerId);

        UserDto stranger = UserDto.builder()
                .name("Чужой")
                .email("stranger@test.com")
                .build();

        UserDto savedStranger = userService.createUser(stranger);

        assertThatThrownBy(() -> bookingService.getBookingById(
                createdBooking.getId(), savedStranger.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("не имеет прав");
    }

    @Test
    void getUserBookings_ShouldReturnAllUserBookings_WhenStateIsAll() {

        for (int i = 1; i <= 3; i++) {
            BookingDto bookingDto = BookingDto.builder()
                    .itemId(itemId)
                    .start(now.plusDays(i))
                    .end(now.plusDays(i + 2))
                    .build();

            bookingService.createBooking(bookingDto, bookerId);
        }

        List<BookingResponseDto> bookings = bookingService.getUserBookings(
                bookerId, "ALL", 0, 10);

        assertThat(bookings).hasSize(3);

        bookings.forEach(booking -> {
            assertThat(booking.getId()).isNotNull();
            assertThat(booking.getStart()).isNotNull();
            assertThat(booking.getEnd()).isNotNull();
            assertThat(booking.getStatus()).isNotNull();
            assertThat(booking.getBooker()).isNotNull();
            assertThat(booking.getBooker().getId()).isNotNull();
            assertThat(booking.getItem()).isNotNull();
            assertThat(booking.getItem().getId()).isNotNull();
        });
    }

    @Test
    void getUserBookings_ShouldReturnFutureBookings_WhenStateIsFuture() {

        BookingDto pastBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .build();
        bookingService.createBooking(pastBooking, bookerId);

        BookingDto futureBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .build();
        BookingResponseDto futureCreated = bookingService.createBooking(futureBooking, bookerId);

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(
                bookerId, "FUTURE", 0, 10);

        assertThat(futureBookings).hasSize(1);
        assertThat(futureBookings.get(0).getId()).isEqualTo(futureCreated.getId());
    }

    @Test
    void getUserBookings_ShouldReturnPastBookings_WhenStateIsPast() {

        BookingDto pastBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .build();
        BookingResponseDto pastCreated = bookingService.createBooking(pastBooking, bookerId);

        BookingDto futureBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .build();
        bookingService.createBooking(futureBooking, bookerId);

        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(
                bookerId, "PAST", 0, 10);

        assertThat(pastBookings).hasSize(1);
        assertThat(pastBookings.get(0).getId()).isEqualTo(pastCreated.getId());
    }

    @Test
    void getUserBookings_ShouldReturnWaitingBookings_WhenStateIsWaiting() {

        BookingDto waitingBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();
        BookingResponseDto waitingCreated = bookingService.createBooking(waitingBooking, bookerId);

        BookingDto approvedBooking = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .build();
        BookingResponseDto approvedCreated = bookingService.createBooking(approvedBooking, bookerId);
        bookingService.approveBooking(approvedCreated.getId(), true, ownerId);

        List<BookingResponseDto> waitingBookings = bookingService.getUserBookings(
                bookerId, "WAITING", 0, 10);

        assertThat(waitingBookings).hasSize(1);
        assertThat(waitingBookings.get(0).getId()).isEqualTo(waitingCreated.getId());
        assertThat(waitingBookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookingsForOwnerItems() {

        BookingDto booking1 = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();
        bookingService.createBooking(booking1, bookerId);

        BookingDto booking2 = BookingDto.builder()
                .itemId(itemId)
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .build();
        bookingService.createBooking(booking2, bookerId);

        List<BookingResponseDto> ownerBookings = bookingService.getOwnerBookings(
                ownerId, "ALL", 0, 10);

        assertThat(ownerBookings).hasSize(2);
    }
}
