package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(ItemRequestDto requestDto, Long userId);

    ItemRequestDto getRequestById(Long requestId, Long userId);

    List<ItemRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId);

    void deleteRequest(Long requestId, Long userId);
}