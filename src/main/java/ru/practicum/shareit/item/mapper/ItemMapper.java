package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        if (itemDto == null) {
            return null;
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .request(request)
                .build();
    }

    public static Item updateFromMap(Item item, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return item;
        }

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value != null && !value.toString().isBlank()) {
                        item.setName(value.toString());
                    }
                    break;
                case "description":
                    if (value != null && !value.toString().isBlank()) {
                        item.setDescription(value.toString());
                    }
                    break;
                case "available":
                    if (value instanceof Boolean) {
                        item.setAvailable((Boolean) value);
                    }
                    break;
            }
        });

        return item;
    }

    public static ItemDto toItemDtoWithComments(Item item, List<CommentDto> comments) {
        ItemDto dto = toItemDto(item);
        if (dto != null) {
            dto.setComments(comments);
        }
        return dto;
    }

    public static ItemDto toItemDtoWithBookings(Item item, BookingResponseDto lastBooking, BookingResponseDto nextBooking, List<CommentDto> comments) {
        ItemDto dto = toItemDto(item);
        if (dto != null) {
            dto.setLastBooking(lastBooking);
            dto.setNextBooking(nextBooking);
            dto.setComments(comments);
        }
        return dto;
    }

}