package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    @Validated(ItemDto.Create.class)
    public ResponseEntity<Object> createItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("POST /items - создание вещи пользователем с ID: {}", userId);
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated(ItemDto.Update.class)
    public ResponseEntity<Object> updateItem(
            @PathVariable @Positive Long itemId,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("PATCH /items/{} - обновление вещи пользователем с ID: {}", itemId, userId);
        return itemClient.updateItem(itemId, updates, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("GET /items/{} - получение вещи по ID пользователем с ID: {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /items - получение всех вещей владельца с ID: {}", userId);
        return itemClient.getAllItemsByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /items/search?text={} - поиск вещей пользователем с ID: {}", text, userId);

        if (text.isBlank()) {
            return ResponseEntity.ok().build();
        }

        return itemClient.searchItems(text, userId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("DELETE /items/{} - удаление вещи пользователем с ID: {}", itemId, userId);
        return itemClient.deleteItem(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CreateCommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        log.info("POST /items/{}/comment - добавление комментария пользователем ID: {}", itemId, userId);
        return itemClient.addComment(itemId, commentDto, userId);
    }
}