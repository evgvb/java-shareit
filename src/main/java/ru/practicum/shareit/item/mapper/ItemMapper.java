package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
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
                .build();
    }

   public static Item toItem(ItemDto itemDto, User owner) {
        if (itemDto == null) {
            return null;
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(owner)
                .build();
    }

    public static Item updateItemFromDto(Item item, ItemUpdateDto updateDto) {
        if (updateDto == null) {
            return item;
        }

        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            item.setName(updateDto.getName());
        }

        if (updateDto.getDescription() != null && !updateDto.getDescription().isBlank()) {
            item.setDescription(updateDto.getDescription());
        }

        if (updateDto.getAvailable() != null) {
            item.setAvailable(updateDto.getAvailable());
        }

        return item;
    }

    public static Item updateItemFromDto(Item item, ItemDto itemDto) {
        if (itemDto == null) {
            return item;
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return item;
    }
}