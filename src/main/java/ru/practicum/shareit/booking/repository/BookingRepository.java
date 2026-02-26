package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findAllByItemOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            Sort sort);

    // Текущие бронирования
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentBookingsByBooker(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Sort sort);

    // Будущие бронирования
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.start > :now")
    List<Booking> findFutureBookingsByBooker(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Sort sort);

    // Прошедшие бронирования
    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.end < :now")
    List<Booking> findPastBookingsByBooker(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Sort sort);

    // бронировал ли пользователь вещь и завершилось ли бронирование
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsByItemIdAndBookerIdAndEndBefore(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // последнее бронирование вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findLastBookingForItem(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);

    // следующее бронирование вещи
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingForItem(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);
}