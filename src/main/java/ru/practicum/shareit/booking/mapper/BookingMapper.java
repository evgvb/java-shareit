package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


@UtilityClass
public class BookingMapper {

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDto.BookerDto bookerDto = null;
        if (booking.getBooker() != null) {
            bookerDto = BookingResponseDto.BookerDto.builder()
                    .id(booking.getBooker().getId())
                    .name(booking.getBooker().getName())
                    .build();
        }
        BookingResponseDto.ItemDto itemDto = null;
        if (booking.getItem() != null) {
            itemDto = BookingResponseDto.ItemDto.builder()
                    .id(booking.getItem().getId())
                    .name(booking.getItem().getName())
                    .description(booking.getItem().getDescription())
                    .available(booking.getItem().getAvailable())
                    .ownerId(booking.getItem().getOwner() != null ? booking.getItem().getOwner().getId() : null)
                    .build();
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        if (bookingDto == null) {
            return null;
        }

        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(bookingDto.getStatus() != null ? bookingDto.getStatus() : BookingStatus.WAITING)
                .build();
    }
}