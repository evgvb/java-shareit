package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

        private BookingRepository bookingRepository;

        private User owner;
        private User booker;
        private Item item;
        private LocalDateTime now;

        @BeforeEach
        void setUp() {

            bookingRepository = new BookingRepository();

            owner = User.builder()
                    .id(1L)
                    .name("Owner")
                    .email("owner@email.com")
                    .build();

            booker = User.builder()
                    .id(2L)
                    .name("User 1")
                    .email("user1@email.com")
                    .build();

            item = Item.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("item 1 описание")
                    .available(true)
                    .owner(owner)
                    .build();

            now = LocalDateTime.now();
        }

        // сохранение нового бронирования
        @Test
        void save_shouldSaveNewBookingAndAssignId() {
            Booking booking = Booking.builder()
                    .start(now.plusDays(1))
                    .end(now.plusDays(2))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            assertNotNull(savedBooking.getId(), "Сохраненному бронированию должен быть присвоен ID");
            assertEquals(BookingStatus.WAITING, savedBooking.getStatus(), "Статус должен сохраниться");
            assertEquals(item.getId(), savedBooking.getItem().getId(), "ID вещи должно сохраниться");
            assertEquals(booker.getId(), savedBooking.getBooker().getId(), "ID бронировщика должно сохраниться");
        }

        // получение всех бронирований пользователя
        @Test
        void findAllByBookerId_shouldReturnAllUserBookings() {
            Booking booking1 = Booking.builder()
                    .start(now.plusDays(1))
                    .end(now.plusDays(2))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();

            Booking booking2 = Booking.builder()
                    .start(now.plusDays(3))
                    .end(now.plusDays(4))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.APPROVED)
                    .build();

            bookingRepository.save(booking1);
            bookingRepository.save(booking2);

            List<Booking> bookings = bookingRepository.findAllByBookerId(booker.getId());

            assertEquals(2, bookings.size(), "Должно вернуться 2 бронирования пользователя");

            // Проверяем сортировку по дате начала (от новых к старым)
            assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart()),"Бронирования должны быть отсортированы от новых к старым");
        }

        // получение бронирований владельца вещей
        @Test
        void findAllByOwnerId_shouldReturnOwnerBookings() {
            Item item2 = Item.builder()
                    .id(2L)
                    .name("Item 2")
                    .description("item 2 описание")
                    .available(true)
                    .owner(owner)
                    .build();

            // Создаем бронирования для обеих вещей
            Booking booking1 = Booking.builder()
                    .start(now.plusDays(1))
                    .end(now.plusDays(2))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();

            Booking booking2 = Booking.builder()
                    .start(now.plusDays(3))
                    .end(now.plusDays(4))
                    .item(item2)
                    .booker(booker)
                    .status(BookingStatus.APPROVED)
                    .build();

            bookingRepository.save(booking1);
            bookingRepository.save(booking2);

            // id вещей владельца
            List<Long> itemIds = List.of(item.getId(), item2.getId());

            // все бронирования владельца
            List<Booking> bookings = bookingRepository.findAllByOwnerId(owner.getId(), itemIds);

            assertEquals(2, bookings.size(), "Должны вернуться все бронирования вещей владельца");
        }

        // поиск бронирований по статусу
        @Test
        void findByBookerIdAndStatus_shouldReturnBookingsWithSpecificStatus() {
            Booking waitingBooking = Booking.builder()
                    .start(now.plusDays(1))
                    .end(now.plusDays(2))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();

            Booking approvedBooking = Booking.builder()
                    .start(now.plusDays(3))
                    .end(now.plusDays(4))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.APPROVED)
                    .build();

            bookingRepository.save(waitingBooking);
            bookingRepository.save(approvedBooking);

            List<Booking> waitingBookings = bookingRepository.findByBookerIdAndStatus(
                    booker.getId(), BookingStatus.WAITING);

            assertEquals(1, waitingBookings.size(), "Должно вернуться одно бронирование со статусом WAITING");
            assertEquals(BookingStatus.WAITING, waitingBookings.get(0).getStatus(),
                    "Статус должен быть WAITING");
        }

        // удаление бронирования
        @Test
        void deleteById_shouldRemoveBookingFromRepository() {
            Booking booking = Booking.builder()
                    .start(now.plusDays(1))
                    .end(now.plusDays(2))
                    .item(item)
                    .booker(booker)
                    .status(BookingStatus.WAITING)
                    .build();
            Booking savedBooking = bookingRepository.save(booking);

            assertTrue(bookingRepository.findById(savedBooking.getId()).isPresent(),
                    "Бронирование должно существовать до удаления");

            bookingRepository.deleteById(savedBooking.getId());

            assertTrue(bookingRepository.findById(savedBooking.getId()).isEmpty(),
                    "После удаления бронирования нет");
        }
    }
