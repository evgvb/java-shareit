package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Map;

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
}