package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи пользователем с ID: {}", ownerId);

        User owner = findUserById(ownerId);

        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item savedItem = itemRepository.save(item);

        log.debug("Вещь создана с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Map<String, Object> updates, Long ownerId) {
        log.info("Обновление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item item = findItemById(itemId);
        verificationOwnerItem(item, ownerId);

        Item updatedItem = ItemMapper.updateFromMap(item, updates);
        Item savedItem = itemRepository.save(updatedItem);

        log.debug("Вещь с ID {} обновлена", itemId);

        return enhanceItemDtoWithBookingsAndComments(savedItem, ownerId);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи с ID: {} пользователем с ID: {}", itemId, userId);

        Item item = findItemById(itemId);

        return enhanceItemDtoWithBookingsAndComments(item, userId);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId, Integer from, Integer size) {
        log.info("Получение всех вещей владельца с ID: {}", ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new NoSuchElementException("Пользователь с ID " + ownerId + " не найден");
        }

        // Получаем все вещи владельца (2 запрос)
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);

        // Применяем пагинацию
        List<Item> paginatedItems = items.stream()
                .skip(from)
                .limit(size)
                .toList();

        // Если нет вещей, возвращаем пустой список
        if (paginatedItems.isEmpty()) {
            return List.of();
        }

        // Собираем ID вещей для запросов
        List<Long> itemIds = paginatedItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        // Получаем все бронирования для этих вещей (3 запрос)
        List<Booking> allBookings = bookingRepository.findAllApprovedByItemIds(itemIds);

        // Группируем бронирования по ID вещи
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Получаем все комментарии для этих вещей (4 запрос)
        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);

        // Группируем комментарии по ID вещи
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        // Формируем результат, используя данные из мап
        return paginatedItems.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), List.of());
                    List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());

                    return enhanceItemDtoWithBookingsAndCommentsFromMaps(
                            item,
                            itemBookings,
                            itemComments,
                            now,
                            ownerId
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId, Integer from, Integer size) {
        log.info("Поиск вещей по тексту: '{}' пользователем с ID: {}", text, userId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Item> items = itemRepository.searchAvailableItems(text);

        List<Item> paginatedItems = items.stream()
                .skip(from)
                .limit(size)
                .toList();

        return paginatedItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long ownerId) {
        log.info("Удаление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item item = findItemById(itemId);
        verificationOwnerItem(item, ownerId);

        itemRepository.deleteById(itemId);
        log.info("Вещь с ID {} удалена", itemId);
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
    }

    private void verificationOwnerItem(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + item.getId() + " не найдена у пользователя с ID " + ownerId);
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CreateCommentDto commentDto, Long authorId) {
        log.info("Добавление комментария к вещи ID: {} пользователем ID: {}", itemId, authorId);

        Item item = findItemById(itemId);

        User author = findUserById(authorId);

        // Проверяем, что пользователь брал и вернул эту вещь
        LocalDateTime now = LocalDateTime.now();
        boolean hasBookedAndFinished = bookingRepository
                .existsByItemIdAndBookerIdAndEndBefore(itemId, authorId, now);

        if (!hasBookedAndFinished) {
            throw new ValidationException("Вы можете оставить комментарий только после завершения бронирования");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий добавлен с ID: {}", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment);
    }

    private ItemDto enhanceItemDtoWithBookingsAndComments(Item item, Long userId) {
        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        // Для владельца добавляем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item.getId(), now);
            List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item.getId(), now);

            BookingResponseDto lastBooking = lastBookings.isEmpty() ? null :
                    BookingMapper.toBookingResponseDto(lastBookings.get(0));
            BookingResponseDto nextBooking = nextBookings.isEmpty() ? null :
                    BookingMapper.toBookingResponseDto(nextBookings.get(0));

            return ItemMapper.toItemDtoWithBookings(item, lastBooking, nextBooking, comments);
        }

        return ItemMapper.toItemDtoWithComments(item, comments);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }

    private ItemDto enhanceItemDtoWithBookingsAndCommentsFromMaps(
            Item item,
            List<Booking> itemBookings,
            List<Comment> itemComments,
            LocalDateTime now,
            Long userId) {

        List<CommentDto> commentDtos = itemComments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> pastBookings = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .sorted((b1, b2) -> b2.getEnd().compareTo(b1.getEnd()))
                    .toList();

            List<Booking> futureBookings = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList();

            BookingResponseDto lastBooking = pastBookings.isEmpty() ? null :
                    BookingMapper.toBookingResponseDto(pastBookings.get(0));
            BookingResponseDto nextBooking = futureBookings.isEmpty() ? null :
                    BookingMapper.toBookingResponseDto(futureBookings.get(0));

            return ItemMapper.toItemDtoWithBookings(item, lastBooking, nextBooking, commentDtos);
        }

        return ItemMapper.toItemDtoWithComments(item, commentDtos);
    }
}