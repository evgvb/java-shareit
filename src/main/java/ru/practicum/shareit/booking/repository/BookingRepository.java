package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {

    private final Map<Long, Booking> bookings = new HashMap<>();
    private final Map<Long, List<Booking>> userBookings = new HashMap<>();
    private final Map<Long, List<Booking>> itemBookings = new HashMap<>();
    private Long idCounter = 1L;

    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(idCounter++);
        }

        bookings.put(booking.getId(), booking);

        Long bookerId = booking.getBooker().getId();
        userBookings.computeIfAbsent(bookerId, k -> new ArrayList<>()).add(booking);

        Long itemId = booking.getItem().getId();
        itemBookings.computeIfAbsent(itemId, k -> new ArrayList<>()).add(booking);

        return booking;
    }

    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    public List<Booking> findAllByBookerId(Long bookerId) {
        return userBookings.getOrDefault(bookerId, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findAllByOwnerId(Long ownerId, List<Long> itemIds) {
        return bookings.values().stream()
                .filter(booking -> itemIds.contains(booking.getItem().getId()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findAllByItemId(Long itemId) {
        return itemBookings.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(Collectors.toList());
    }

    public List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now) {
        return userBookings.getOrDefault(bookerId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findPastByBookerId(Long bookerId, LocalDateTime now) {
        return userBookings.getOrDefault(bookerId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getEnd().isBefore(now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findFutureByBookerId(Long bookerId, LocalDateTime now) {
        return userBookings.getOrDefault(bookerId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status) {
        return userBookings.getOrDefault(bookerId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getStatus() == status)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, List<Long> itemIds) {
        return bookings.values().stream()
                .filter(booking -> itemIds.contains(booking.getItem().getId()))
                .filter(booking -> booking.getStatus() == status)
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    public boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime now) {
        return bookings.values().stream()
                .anyMatch(booking ->
                        booking.getItem().getId().equals(itemId) &&
                                booking.getBooker().getId().equals(bookerId) &&
                                booking.getEnd().isBefore(now) &&
                                booking.getStatus() == BookingStatus.APPROVED);
    }

    public Optional<Booking> findLastBookingForItem(Long itemId, LocalDateTime now) {
        return itemBookings.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getEnd().isBefore(now))
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                .max(Comparator.comparing(Booking::getEnd));
    }

    public Optional<Booking> findNextBookingForItem(Long itemId, LocalDateTime now) {
        return itemBookings.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .filter(booking -> booking.getStatus() == BookingStatus.APPROVED)
                .min(Comparator.comparing(Booking::getStart));
    }

    public Booking update(Booking booking) {
        if (booking.getId() == null || !bookings.containsKey(booking.getId())) {
            throw new NoSuchElementException("Бронирование не найдено");
        }

        bookings.put(booking.getId(), booking);
        return booking;
    }

    public void deleteById(Long id) {
        Booking booking = bookings.remove(id);
        if (booking != null) {
            Long bookerId = booking.getBooker().getId();
            List<Booking> userBookingsList = userBookings.get(bookerId);
            if (userBookingsList != null) {
                userBookingsList.remove(booking);
            }

            Long itemId = booking.getItem().getId();
            List<Booking> itemBookingsList = itemBookings.get(itemId);
            if (itemBookingsList != null) {
                itemBookingsList.remove(booking);
            }
        }
    }
}