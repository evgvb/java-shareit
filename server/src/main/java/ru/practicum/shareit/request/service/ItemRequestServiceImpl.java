package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
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
        List<Item> items = itemRepository.findAllByRequestId(requestId);

        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, itemDtos);
    }

    @Override
    public List<ItemRequestDto> getUserItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение запросов пользователя ID: {}", userId);

        findUserById(userId);

        // 1 запрос: получаем запросы пользователя
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(userId, SORT_BY_CREATED_DESC);
        log.debug("Найдено {} запросов пользователя ID {}", requests.size(), userId);

        // Применяем пагинацию до запроса вещей
        List<ItemRequest> paginatedRequests = requests.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());

        if (paginatedRequests.isEmpty()) {
            return List.of();
        }

        // Собираем ID запросов
        List<Long> requestIds = paginatedRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // 2 запрос: получаем все вещи для этих запросов одним запросом
        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);
        log.debug("Найдено {} вещей для {} запросов", allItems.size(), requestIds.size());

        // Группируем вещи по ID запроса
        Map<Long, List<Item>> itemsByRequestId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        // Формируем результат, используя данные из мапы
        return paginatedRequests.stream()
                .map(request -> {
                    List<Item> itemsForRequest = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    List<ItemDto> itemDtos = itemsForRequest.stream()
                            .map(ItemMapper::toItemDto)
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestDtoWithItems(request, itemDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов, кроме запросов пользователя ID: {}", userId);

        findUserById(userId);

        // 1 запрос: получаем все запросы других пользователей
        List<ItemRequest> requests = itemRequestRepository.findAllExceptRequestor(userId, SORT_BY_CREATED_DESC);
        log.debug("Найдено {} запросов других пользователей", requests.size());

        // Применяем пагинацию до запроса вещей
        List<ItemRequest> paginatedRequests = requests.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());

        if (paginatedRequests.isEmpty()) {
            return List.of();
        }

        // Собираем ID запросов
        List<Long> requestIds = paginatedRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // 2 запрос: получаем все вещи для этих запросов одним запросом
        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);
        log.debug("Найдено {} вещей для {} запросов", allItems.size(), requestIds.size());

        // Группируем вещи по ID запроса
        Map<Long, List<Item>> itemsByRequestId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        // Формируем результат, используя данные из мапы
        return paginatedRequests.stream()
                .map(request -> {
                    List<Item> itemsForRequest = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    List<ItemDto> itemDtos = itemsForRequest.stream()
                            .map(ItemMapper::toItemDto)
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestDtoWithItems(request, itemDtos);
                })
                .collect(Collectors.toList());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new NoSuchElementException("Пользователь с ID " + userId + " не найден");
                });
    }
}