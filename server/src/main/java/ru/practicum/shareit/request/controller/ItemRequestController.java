package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Добавляем статус 201
    public ItemRequestDto createItemRequest(
            @RequestBody CreateItemRequestDto createDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("POST /requests - создание запроса вещи пользователем ID: {}", userId);
        return itemRequestService.createItemRequest(createDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserItemRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /requests - получение запросов пользователя ID: {}", userId);
        return itemRequestService.getUserItemRequests(userId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /requests/all - получение всех запросов пользователем ID: {}", userId);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests/{} - получение запроса по ID пользователем ID: {}", requestId, userId);
        return itemRequestService.getItemRequestById(requestId, userId);
    }
}