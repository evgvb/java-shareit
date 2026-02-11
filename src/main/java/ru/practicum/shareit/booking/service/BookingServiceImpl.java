package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(BookingDto bookingDto, Long userId) {
        log.info("Создание бронирования пользователем с ID: {}", userId);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NoSuchElementException("Владелец не может бронировать свою вещь");
        }

        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование создано с ID: {}", savedBooking.getId());
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        log.info("Подтверждение бронирования с ID: {} владельцем с ID: {}, approved: {}",
                bookingId, ownerId, approved);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.update(booking);

        log.info("Бронирование с ID {} {}",
                bookingId, approved ? "подтверждено" : "отклонено");

        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.info("Получение бронирования с ID: {} пользователем с ID: {}", bookingId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NoSuchElementException("Доступ запрещен");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state) {
        log.info("Получение бронирований пользователя с ID: {}, состояние: {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findAllByBookerId(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findCurrentByBookerId(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findPastByBookerId(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureByBookerId(userId, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        log.info("Получение бронирований владельца с ID: {}, состояние: {}", ownerId, state);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        List<Long> itemIds = itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        if (itemIds.isEmpty()) {
            return List.of();
        }

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findAllByOwnerId(ownerId, itemIds);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByOwnerId(ownerId, itemIds)
                        .stream()
                        .filter(booking -> booking.getStart().isBefore(now) &&
                                booking.getEnd().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "PAST":
                bookings = bookingRepository.findAllByOwnerId(ownerId, itemIds)
                        .stream()
                        .filter(booking -> booking.getEnd().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByOwnerId(ownerId, itemIds)
                        .stream()
                        .filter(booking -> booking.getStart().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case "WAITING":
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId,
                        BookingStatus.WAITING, itemIds);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId,
                        BookingStatus.REJECTED, itemIds);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDto cancelBooking(Long bookingId, Long userId) {
        log.info("Отмена бронирования с ID: {} пользователем с ID: {}", bookingId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId)) {
            throw new ValidationException("Только автор бронирования может его отменить");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Можно отменить только ожидающее бронирование");
        }

        booking.setStatus(BookingStatus.CANCELED);
        Booking updatedBooking = bookingRepository.update(booking);

        log.info("Бронирование с ID {} отменено", bookingId);
        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }
    }
}