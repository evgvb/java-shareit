package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingControllerTest extends BaseControllerTest {

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;
    private final Long userId = 1L;
    private final Long ownerId = 2L;
    private final Long bookingId = 1L;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto.BookerDto bookerDto = BookingResponseDto.BookerDto.builder()
                .id(userId)
                .name("user1")
                .build();

        BookingResponseDto.ItemDto itemDto = BookingResponseDto.ItemDto.builder()
                .id(1L)
                .name("штука")
                .description("нужная штука")
                .available(true)
                .ownerId(ownerId)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .status(BookingStatus.WAITING)
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), eq(userId))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(userId))
                .andExpect(jsonPath("$.item.id").value(1));

        verify(bookingService, times(1)).createBooking(any(BookingDto.class), eq(userId));
    }

    @Test
    void approveBooking_ShouldReturnApprovedBooking() throws Exception {
        BookingResponseDto approvedBooking = bookingResponseDto;
        approvedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingService.approveBooking(eq(bookingId), eq(true), eq(ownerId)))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).approveBooking(eq(bookingId), eq(true), eq(ownerId));
    }

    @Test
    void approveBooking_ShouldReturnRejectedBooking() throws Exception {
        BookingResponseDto rejectedBooking = bookingResponseDto;
        rejectedBooking.setStatus(BookingStatus.REJECTED);

        when(bookingService.approveBooking(eq(bookingId), eq(false), eq(ownerId)))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService, times(1)).approveBooking(eq(bookingId), eq(false), eq(ownerId));
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserIsBooker() throws Exception {
        when(bookingService.getBookingById(bookingId, userId)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.booker.id").value(userId));

        verify(bookingService, times(1)).getBookingById(bookingId, userId);
    }

    @Test
    void getBookingById_ShouldReturnBooking_WhenUserIsOwner() throws Exception {
        when(bookingService.getBookingById(bookingId, ownerId)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.item.ownerId").value(ownerId));

        verify(bookingService, times(1)).getBookingById(bookingId, ownerId);
    }

    @Test
    void getBookingById_ShouldReturn404_WhenBookingNotFound() throws Exception {
        when(bookingService.getBookingById(999L, userId))
                .thenThrow(new NoSuchElementException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingById(999L, userId);
    }

    @Test
    void getUserBookings_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(bookingResponseDto,
                BookingResponseDto.builder().id(2L).start(now.plusDays(5)).end(now.plusDays(7)).build());

        when(bookingService.getUserBookings(eq(userId), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingService, times(1)).getUserBookings(eq(userId), eq("ALL"), eq(0), eq(10));
    }

    @Test
    void getUserBookings_WithDifferentStates_ShouldCallService() throws Exception {
        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            when(bookingService.getUserBookings(eq(userId), eq(state), anyInt(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", userId)
                            .param("state", state))
                    .andExpect(status().isOk());

            verify(bookingService, times(1)).getUserBookings(eq(userId), eq(state), eq(0), eq(10));
        }
    }

    @Test
    void getOwnerBookings_ShouldReturnListOfBookings() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(bookingResponseDto);

        when(bookingService.getOwnerBookings(eq(ownerId), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingId));

        verify(bookingService, times(1)).getOwnerBookings(eq(ownerId), eq("ALL"), eq(0), eq(10));
    }

    @Test
    void getOwnerBookings_WithDifferentStates_ShouldCallService() throws Exception {
        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};

        for (String state : states) {
            when(bookingService.getOwnerBookings(eq(ownerId), eq(state), anyInt(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", ownerId)
                            .param("state", state))
                    .andExpect(status().isOk());

            verify(bookingService, times(1)).getOwnerBookings(eq(ownerId), eq(state), eq(0), eq(10));
        }
    }

    @Test
    void getUserBookings_WithPagination_ShouldPassParameters() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getUserBookings(eq(userId), eq("ALL"), eq(5), eq(20));
    }
}