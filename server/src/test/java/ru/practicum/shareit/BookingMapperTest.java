package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBookingResponseDto_ShouldConvertBookingToDto() {
        User booker = User.builder().id(1L).name("арендатор").build();
        User owner = User.builder().id(2L).name("владелец").build();
        Item item = Item.builder().id(10L).name("вещь").description("описание").available(true).owner(owner).build();
        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .id(100L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getBooker().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void toBookingResponseDto_ShouldReturnNull_WhenBookingIsNull() {
        assertThat(BookingMapper.toBookingResponseDto(null)).isNull();
    }

    @Test
    void toBooking_ShouldConvertDtoToBooking() {
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(10L).build();
        LocalDateTime now = LocalDateTime.now();
        BookingDto dto = BookingDto.builder()
                .id(100L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(10L)
                .bookerId(1L)
                .status(BookingStatus.APPROVED)
                .build();

        Booking booking = BookingMapper.toBooking(dto, item, booker);

        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isEqualTo(100L);
        assertThat(booking.getItem()).isEqualTo(item);
        assertThat(booking.getBooker()).isEqualTo(booker);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void toBooking_ShouldSetDefaultStatusWaiting_WhenStatusIsNull() {
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(10L).build();
        LocalDateTime now = LocalDateTime.now();
        BookingDto dto = BookingDto.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .itemId(10L)
                .build();

        Booking booking = BookingMapper.toBooking(dto, item, booker);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}