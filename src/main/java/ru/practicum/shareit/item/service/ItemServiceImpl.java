package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи пользователем с ID: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + ownerId + " не найден"));

        // Проверяем запрос, если указан
        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь создана с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        log.info("Обновление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        // Проверяем, что пользователь является владельцем - если нет, то 404
        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, itemDto);
        Item savedItem = itemRepository.update(updatedItem);

        log.info("Вещь с ID {} обновлена", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    public ItemDto partialUpdateItem(Long itemId, ItemUpdateDto updateDto, Long ownerId) {

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, updateDto);
        Item savedItem = itemRepository.update(updatedItem);

        log.info("Вещь с ID {} частично обновлена", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи с ID: {} пользователем с ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца с ID: {}", ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new NoSuchElementException("Пользователь с ID " + ownerId + " не найден");
        }

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId) {
        log.info("Поиск вещей по тексту: '{}' пользователем с ID: {}", text, userId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        log.info("Удаление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        itemRepository.deleteById(itemId);
        log.info("Вещь с ID {} удалена", itemId);
    }

    public ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId) {
        log.info("Получение вещи с бронированиями и комментариями, itemId: {}, userId: {}",
                itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        ItemWithBookingsDto.BookingDto lastBooking = null;
        ItemWithBookingsDto.BookingDto nextBooking = null;

        // Если пользователь - владелец, показываем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository.findLastBookingForItem(itemId, now)
                    .map(booking -> ItemWithBookingsDto.BookingDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .build())
                    .orElse(null);

            nextBooking = bookingRepository.findNextBookingForItem(itemId, now)
                    .map(booking -> ItemWithBookingsDto.BookingDto.builder()
                            .id(booking.getId())
                            .bookerId(booking.getBooker().getId())
                            .build())
                    .orElse(null);
        }

        // Получаем комментарии
        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    public List<ItemWithBookingsDto> getAllItemsWithBookingsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца с бронированиями и комментариями, ownerId: {}",
                ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        List<Item> items = itemRepository.findAllByOwnerId(ownerId);

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Получаем все бронирования для этих вещей
        LocalDateTime now = LocalDateTime.now();

        // Получаем все комментарии для этих вещей
        Map<Long, List<Comment>> commentsByItem = commentRepository.findAllByItemIds(itemIds);

        return items.stream()
                .map(item -> {
                    // Получаем последнее и следующее бронирование
                    ItemWithBookingsDto.BookingDto lastBooking =
                            bookingRepository.findLastBookingForItem(item.getId(), now)
                                    .map(booking -> ItemWithBookingsDto.BookingDto.builder()
                                            .id(booking.getId())
                                            .bookerId(booking.getBooker().getId())
                                            .build())
                                    .orElse(null);

                    ItemWithBookingsDto.BookingDto nextBooking =
                            bookingRepository.findNextBookingForItem(item.getId(), now)
                                    .map(booking -> ItemWithBookingsDto.BookingDto.builder()
                                            .id(booking.getId())
                                            .bookerId(booking.getBooker().getId())
                                            .build())
                                    .orElse(null);

                    // Получаем комментарии для этой вещи
                    List<CommentDto> comments = commentsByItem.getOrDefault(item.getId(),
                                    Collections.emptyList())
                            .stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList());

                    return ItemWithBookingsDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                            .lastBooking(lastBooking)
                            .nextBooking(nextBooking)
                            .comments(comments)
                            .build();
                })
                .collect(Collectors.toList());
    }
}