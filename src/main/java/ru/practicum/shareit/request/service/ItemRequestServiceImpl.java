package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto requestDto, Long userId) {
        log.info("Создание запроса пользователем с ID: {}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);

        log.info("Запрос создан с ID: {}", savedRequest.getId());
        return ItemRequestMapper.toItemRequestDto(savedRequest, Collections.emptyList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получение запроса с ID: {} пользователем с ID: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос не найден"));

        List<ru.practicum.shareit.item.model.Item> items =
                itemRepository.findAllByRequestId(requestId);

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение всех запросов пользователя с ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<ru.practicum.shareit.item.model.Item> allItems =
                itemRepository.findAllByRequestIdIn(requestIds);

        return requests.stream()
                .map(request -> {
                    List<ru.practicum.shareit.item.model.Item> itemsForRequest = allItems.stream()
                            .filter(item -> item.getRequest() != null &&
                                    item.getRequest().getId().equals(request.getId()))
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestDto(request, itemsForRequest);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение всех запросов (кроме пользователя с ID: {})", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        List<ItemRequest> allRequests = itemRequestRepository.findAllExceptRequestor(userId);

        List<Long> requestIds = allRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);

        return allRequests.stream()
                .map(request -> {
                    List<Item> itemsForRequest = allItems.stream()
                            .filter(item -> item.getRequest() != null &&
                                    item.getRequest().getId().equals(request.getId()))
                            .collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestDto(request, itemsForRequest);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRequest(Long requestId, Long userId) {
        log.info("Удаление запроса с ID: {} пользователем с ID: {}", requestId, userId);

        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(() -> new NoSuchElementException("Запрос не найден"));

        if (!request.getRequestor().getId().equals(userId)) {
            throw new ValidationException("Только автор может удалить запрос");
        }

        itemRequestRepository.deleteById(requestId);
        log.info("Запрос с ID {} удален", requestId);
    }
}