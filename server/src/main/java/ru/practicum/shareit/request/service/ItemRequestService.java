package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createItemRequest(CreateItemRequestDto createDto, Long requestorId);

    ItemRequestDto getItemRequestById(Long requestId, Long userId);

    List<ItemRequestDto> getUserItemRequests(Long userId, Integer from, Integer size);

    List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size);
}