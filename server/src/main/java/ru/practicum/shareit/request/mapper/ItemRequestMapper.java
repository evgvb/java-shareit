package ru.practicum.shareit.request.mapper;

import lombok.extern.slf4j.Slf4j;
import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@UtilityClass
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            log.debug("ItemRequestMapper: получен null ItemRequest");
            return null;
        }

        log.debug("ItemRequestMapper: конвертация ItemRequest ID={}", itemRequest.getId());

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .requestorId(itemRequest.getRequestor().getId())
                .requestorName(itemRequest.getRequestor().getName())
                .items(Collections.emptyList())
                .build();
    }

    public static ItemRequestDto toItemRequestDtoWithItems(ItemRequest itemRequest, List<ItemDto> items) {
        if (itemRequest == null) {
            log.debug("ItemRequestMapper: получен null ItemRequest для toItemRequestDtoWithItems");
            return null;
        }

        log.debug("ItemRequestMapper: конвертация ItemRequest ID={} с {} вещами",
                itemRequest.getId(), items.size());

        ItemRequestDto dto = toItemRequestDto(itemRequest);
        if (dto != null) {
            dto.setItems(items != null ? items : Collections.emptyList());
            log.debug("ItemRequestMapper: в DTO добавлено {} вещей", dto.getItems().size());
        }
        return dto;
    }

    public static ItemRequest toItemRequest(CreateItemRequestDto createDto, User requestor) {
        if (createDto == null) {
            log.debug("ItemRequestMapper: получен null CreateItemRequestDto");
            return null;
        }

        log.debug("ItemRequestMapper: создание ItemRequest из DTO с description='{}'",
                createDto.getDescription());

        return ItemRequest.builder()
                .description(createDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }
}