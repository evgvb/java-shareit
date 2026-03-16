package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(CreateItemRequestDto createDto, Long requestorId) {
        log.info("Создание нового запроса вещи пользователем ID: {}", requestorId);

        User requestor = findUserById(requestorId);

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(createDto, requestor);

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        log.info("Получение запроса ID: {} пользователем ID: {}", requestId, userId);

        findUserById(userId);

        // Получаем запрос
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Запрос с ID {} не найден", requestId);
                    return new NoSuchElementException("Запрос с ID " + requestId + " не найден");
                });

        // Получаем вещи, связанные с запросом
        log.debug("Ищем вещи с request_id = {}", requestId);
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        log.debug("Найдено {} вещей для запроса ID {}", items.size(), requestId);

        if (!items.isEmpty()) {
            for (Item item : items) {
                log.info("  - Вещь: ID={}, name='{}', ownerId={}",
                        item.getId(), item.getName(), item.getOwner().getId());
            }
        }

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDtoWithItems(itemRequest,
                items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение запросов пользователя ID: {}", userId);

        findUserById(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(userId, SORT_BY_CREATED_DESC);
        log.debug("Найдено {} запросов пользователя ID {}", requests.size(), userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = getItemsByRequestIds(requestIds);

        List<ItemRequestDto> result = requests.stream()
                .skip(from)
                .limit(size)
                .map(request -> {
                    List<Item> items = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    log.debug("  - Запрос ID {}: найдено {} вещей", request.getId(), items.size());
                    return ItemRequestMapper.toItemRequestDtoWithItems(request,
                            items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов, кроме запросов пользователя ID: {}", userId);

        findUserById(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllExceptRequestor(userId, SORT_BY_CREATED_DESC);
        log.info("Найдено {} запросов других пользователей", requests.size());

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = getItemsByRequestIds(requestIds);

        List<ItemRequestDto> result = requests.stream()
                .skip(from)
                .limit(size)
                .map(request -> {
                    List<Item> items = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    log.info("  - Запрос ID {} (от пользователя {}): найдено {} вещей",
                            request.getId(), request.getRequestor().getId(), items.size());
                    return ItemRequestMapper.toItemRequestDtoWithItems(request,
                            items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
        return result;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new NoSuchElementException("Пользователь с ID " + userId + " не найден");
                });
    }

    private Map<Long, List<Item>> getItemsByRequestIds(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return Map.of();
        }

        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);
        log.debug("Найдено {} вещей для {} запросов", allItems.size(), requestIds.size());

        return allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
    }
}