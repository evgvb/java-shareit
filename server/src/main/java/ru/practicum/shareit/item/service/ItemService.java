package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, Map<String, Object> updates, Long ownerId);

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getAllItemsByOwner(Long ownerId, Integer from, Integer size);

    List<ItemDto> searchItems(String text, Long userId, Integer from, Integer size);

    void deleteItem(Long itemId, Long ownerId);

    CommentDto addComment(Long itemId, CreateCommentDto commentDto, Long authorId);
}