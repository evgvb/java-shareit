package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(ItemDto.Create.class)
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("POST /items - создание вещи пользователем с ID: {}", userId);
        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated(ItemDto.Update.class)
    public ItemDto updateItem(
            @PathVariable @Positive Long itemId,
            @RequestBody Map<String, Object> updates,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("PATCH /items/{} - обновление вещи пользователем с ID: {}", itemId, userId);
        return itemService.updateItem(itemId, updates, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("GET /items/{} - получение вещи по ID пользователем с ID: {}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("GET /items - получение всех вещей владельца с ID: {}", userId);
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("GET /items/search?text={} - поиск вещей пользователем с ID: {}", text, userId);
        return itemService.searchItems(text, userId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable @Positive Long itemId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        log.info("DELETE /items/{} - удаление вещи пользователем с ID: {}", itemId, userId);
        itemService.deleteItem(itemId, userId);
    }
}