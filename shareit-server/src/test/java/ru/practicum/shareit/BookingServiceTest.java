package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        booker = User.builder()
                .id(1L)
                .name("владелец")
                .email("booker@email.com")
                .build();

        owner = User.builder()
                .id(2L)
                .name("арендатор")
                .email("owner@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("item description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна");
    }

    @Test
    void createBooking_WhenOwnerTriesToBookOwnItem_ShouldThrowNoSuchElementException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, owner.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Владелец не может забронировать свою вещь");
    }

    @Test
    void createBooking_WhenEndDateBeforeStart_ShouldThrowValidationException() {
        bookingDto.setEnd(now.minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("должна быть больше");
    }

    @Test
    void createBooking_WhenEndDateEqualsStart_ShouldThrowValidationException() {
        bookingDto.setEnd(bookingDto.getStart());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("должна быть больше");
    }

    @Test
    void approveBooking_WhenUserNotOwner_ShouldThrowAccessDeniedException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, true, 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("не является владельцем");
    }

    @Test
    void approveBooking_WhenBookingNotWaiting_ShouldThrowValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, true, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("уже обработано");
    }

    @Test
    void approveBooking_WhenApproved_ShouldSetStatusApproved() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approveBooking(1L, true, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_WhenRejected_ShouldSetStatusRejected() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approveBooking(1L, false, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void getBookingById_WhenUserNotAuthorOrOwner_ShouldThrowAccessDeniedException() {
        User stranger = User.builder().id(999L).build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("не имеет прав");
    }

    @Test
    void getUserBookings_WithInvalidFromParameter_ShouldThrowValidationException() {
        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "ALL", -1, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("from");
    }

    @Test
    void getUserBookings_WithInvalidSizeParameter_ShouldThrowValidationException() {
        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "ALL", 0, 0))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("size");
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowValidationException() {
        assertThatThrownBy(() -> bookingService.getUserBookings(1L, "INVALID_STATE", 0, 10))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не определен");
    }

    @Test
    void getOwnerBookings_WhenUserNotFound_ShouldThrowNoSuchElementException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getOwnerBookings(999L, "ALL", 0, 10))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }
}