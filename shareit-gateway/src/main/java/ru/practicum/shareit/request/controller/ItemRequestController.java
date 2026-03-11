package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(
            @Valid @RequestBody CreateItemRequestDto createDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("POST /requests - создание запроса вещи пользователем ID: {}", userId);
        return itemRequestClient.createItemRequest(createDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItemRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /requests - получение запросов пользователя ID: {}", userId);
        return itemRequestClient.getUserItemRequests(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /requests/all - получение всех запросов пользователем ID: {}", userId);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(
            @PathVariable @Positive Long requestId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("GET /requests/{} - получение запроса по ID пользователем ID: {}", requestId, userId);
        return itemRequestClient.getItemRequestById(requestId, userId);
    }
}