package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
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
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto, Long bookerId) {
        log.info("Создание нового бронирования пользователем ID: {}", bookerId);

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + bookerId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + bookingDto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с ID " + item.getId() + " недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NoSuchElementException("Владелец не может забронировать свою вещь");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().isEqual(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть больше даты начала");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование создано с ID: {}", savedBooking.getId());
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        log.info("Подтверждение/отклонение бронирования ID: {} владельцем ID: {}", bookingId, ownerId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Пользователь ID {} не является владельцем вещи ID {}", ownerId, booking.getItem().getId());
            throw new AccessDeniedException(
                    String.format("Пользователь с ID %d не является владельцем вещи. Подтверждать бронирование может только владелец", ownerId)
            );
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано. Текущий статус: " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Бронирование ID: {} обновлено, статус: {}", bookingId, booking.getStatus());
        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.info("Получение бронирования ID: {} пользователем ID: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + bookingId + " не найдено"));

        // пользователь не автор/владелец
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь ID {} не имеет прав на просмотр бронирования ID {}", userId, bookingId);
            throw new AccessDeniedException(
                    String.format("Пользователь с ID %d не имеет прав на просмотр этого бронирования", userId)
            );
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований пользователя ID: {} с состоянием: {}", userId, state);

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();

        ///Pageable pageable = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        List<Booking> bookings;

        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());

            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findCurrentBookingsByBooker(userId, now, SORT_BY_START_DESC);
                    break;
                case PAST:
                    bookings = bookingRepository.findPastBookingsByBooker(userId, now, SORT_BY_START_DESC);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findFutureBookingsByBooker(userId, now, SORT_BY_START_DESC);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                    break;
                default:
                    throw new ValidationException("Статус не определен: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Статус не определен: " + state);
        }

        List<Booking> paginatedBookings = bookings.stream()
                .skip(from)
                .limit(size)
                .toList();

        return paginatedBookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, Integer from, Integer size) {
        log.info("Получение бронирований для вещей владельца ID: {} с состоянием: {}", ownerId, state);

        // Проверяем существование пользователя
        if (!userRepository.existsById(ownerId)) {
            throw new NoSuchElementException("Пользователь с ID " + ownerId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();

        ///Pageable pageable = PageRequest.of(from / size, size, SORT_BY_START_DESC);

        List<Booking> bookings;

        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());

            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findAllByItemOwnerId(ownerId, SORT_BY_START_DESC);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findCurrentBookingsByBooker(ownerId, now, SORT_BY_START_DESC);
                    break;
                case PAST:
                    bookings = bookingRepository.findPastBookingsByBooker(ownerId, now, SORT_BY_START_DESC);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findFutureBookingsByBooker(ownerId, now, SORT_BY_START_DESC);
                    break;
                case WAITING:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, SORT_BY_START_DESC);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                    break;
                default:
                    throw new ValidationException("Статус не определен: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Статус не определен: " + state);
        }

        List<Booking> paginatedBookings = bookings.stream()
                .skip(from)
                .limit(size)
                .toList();

        return paginatedBookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    private enum BookingState {
        ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED
    }
}