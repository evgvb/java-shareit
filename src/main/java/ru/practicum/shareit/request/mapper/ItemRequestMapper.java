package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        if (request == null) {
            return null;
        }

        List<ItemDto> itemDtos = items != null ?
                items.stream()
                        .map(item -> ItemDto.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                                .build())
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto, User requestor) {
        if (requestDto == null) {
            return null;
        }

        return ItemRequest.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(requestDto.getCreated() != null ? requestDto.getCreated() : null)
                .build();
    }
}