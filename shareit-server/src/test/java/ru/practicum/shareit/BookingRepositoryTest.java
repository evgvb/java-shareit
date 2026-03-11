package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = userRepository.save(User.builder().name("владелец").email("owner@email.com").build());
        booker = userRepository.save(User.builder().name("арендатор").email("booker@email.com").build());

        item = itemRepository.save(Item.builder()
                .name("штука")
                .description("нужная штука")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void findCurrentBookingsByBooker_ShouldReturnCurrentBookings() {
        Booking pastBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(5)).end(now.minusDays(3)).item(item).booker(booker).status(BookingStatus.APPROVED).build());
        Booking currentBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(1)).end(now.plusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());
        Booking futureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(1)).end(now.plusDays(3)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        List<Booking> currentBookings = bookingRepository.findCurrentBookingsByBooker(booker.getId(), now, Sort.by(Sort.Direction.ASC, "start"));

        assertThat(currentBookings).hasSize(1);
        assertThat(currentBookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void findLastBookingForItem_ShouldReturnAllPastApprovedBookingsOrderedByEndDesc() {
        // Создаем два прошедших бронирования с разными датами окончания
        Booking olderBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(10))
                .end(now.minusDays(8))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        Booking newerBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // Создаем будущее бронирование (не должно попасть в результат)
        Booking futureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // Создаем прошедшее бронирование с другим статусом (не должно попасть в результат)
        Booking rejectedBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(7))
                .end(now.minusDays(5))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build());

        List<Booking> result = bookingRepository.findLastBookingForItem(item.getId(), now);

        // Метод должен вернуть все APPROVED прошедшие бронирования, отсортированные по end DESC
        assertThat(result).hasSize(2);

        // Первым должно быть более позднее бронирование (newerBooking)
        assertThat(result.get(0).getId()).isEqualTo(newerBooking.getId());
        assertThat(result.get(1).getId()).isEqualTo(olderBooking.getId());

        // Проверяем, что будущие и не-APPROVED бронирования не попали в результат
        assertThat(result).doesNotContain(futureBooking, rejectedBooking);
    }

    @Test
    void findNextBookingForItem_ShouldReturnAllFutureApprovedBookingsOrderedByStartAsc() {
        // Создаем два будущих бронирования с разными датами начала
        Booking earlierFutureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        Booking laterFutureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(5))
                .end(now.plusDays(7))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // Создаем прошедшее бронирование (не должно попасть в результат)
        Booking pastBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // Создаем будущее бронирование с другим статусом (не должно попасть в результат)
        Booking waitingBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(2))
                .end(now.plusDays(4))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build());

        List<Booking> result = bookingRepository.findNextBookingForItem(item.getId(), now);

        // Метод должен вернуть все APPROVED будущие бронирования, отсортированные по start ASC
        assertThat(result).hasSize(2);

        // Первым должно быть более раннее будущее бронирование
        assertThat(result.get(0).getId()).isEqualTo(earlierFutureBooking.getId());
        assertThat(result.get(1).getId()).isEqualTo(laterFutureBooking.getId());

        // Проверяем, что прошедшие и не-APPROVED бронирования не попали в результат
        assertThat(result).doesNotContain(pastBooking, waitingBooking);
    }

    @Test
    void existsByItemIdAndBookerIdAndEndBefore_ShouldReturnTrue_WhenBookingExists() {
        bookingRepository.save(Booking.builder()
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        boolean exists = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(item.getId(), booker.getId(), now);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByItemIdAndBookerIdAndEndBefore_ShouldReturnFalse_WhenStatusIsNotApproved() {
        bookingRepository.save(Booking.builder()
                .start(now.minusDays(5))
                .end(now.minusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build());

        boolean exists = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(item.getId(), booker.getId(), now);

        assertThat(exists).isFalse();
    }
}