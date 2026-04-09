package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemMapperTest {
    @Test
    void toItemDto_WhenItemNull_ShouldReturnNull() {
        assertThat(ItemMapper.toItemDto(null)).isNull();
    }

    @Test
    void toItem_WhenItemDtoNull_ShouldReturnNull() {
        assertThat(ItemMapper.toItem(null, null, null)).isNull();
    }

    @Test
    void toItem_WithRequestNull_ShouldCreateItemWithoutRequest() {
        User owner = User.builder().id(1L).build();
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("item description")
                .available(true)
                .build();

        Item result = ItemMapper.toItem(itemDto, owner, null);

        assertThat(result).isNotNull();
        assertThat(result.getRequest()).isNull();
    }

    @Test
    void updateFromMap_WithNullMap_ShouldReturnOriginalItem() {
        Item item = Item.builder().id(1L).name("Original").build();

        Item result = ItemMapper.updateFromMap(item, null);

        assertThat(result).isSameAs(item);
        assertThat(result.getName()).isEqualTo("Original");
    }

    @Test
    void updateFromMap_WithEmptyMap_ShouldReturnOriginalItem() {
        Item item = Item.builder().id(1L).name("Original").build();

        Item result = ItemMapper.updateFromMap(item, Map.of());

        assertThat(result).isSameAs(item);
        assertThat(result.getName()).isEqualTo("Original");
    }

    @Test
    void updateFromMap_WithBlankName_ShouldNotUpdateName() {
        Item item = Item.builder().id(1L).name("Original").build();
        Map<String, Object> updates = Map.of("name", "   ");

        Item result = ItemMapper.updateFromMap(item, updates);

        assertThat(result.getName()).isEqualTo("Original");
    }

    @Test
    void updateFromMap_WithBlankDescription_ShouldNotUpdateDescription() {
        Item item = Item.builder().id(1L).description("Original").build();
        Map<String, Object> updates = Map.of("item description", "   ");

        Item result = ItemMapper.updateFromMap(item, updates);

        assertThat(result.getDescription()).isEqualTo("Original");
    }

    @Test
    void updateFromMap_WithNonBooleanAvailable_ShouldNotUpdateAvailable() {
        Item item = Item.builder().id(1L).available(true).build();
        Map<String, Object> updates = Map.of("available", "not boolean");

        Item result = ItemMapper.updateFromMap(item, updates);

        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void toItemDtoWithComments_WhenItemNull_ShouldReturnNull() {
        assertThat(ItemMapper.toItemDtoWithComments(null, List.of())).isNull();
    }

    @Test
    void toItemDtoWithBookings_WhenItemNull_ShouldReturnNull() {
        assertThat(ItemMapper.toItemDtoWithBookings(null, null, null, null)).isNull();
    }

    @Test
    void toItemDtoWithBookings_ShouldSetAllFields() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("item description")
                .available(true)
                .owner(User.builder().id(1L).build())
                .build();

        BookingResponseDto lastBooking = BookingResponseDto.builder().id(1L).build();
        BookingResponseDto nextBooking = BookingResponseDto.builder().id(2L).build();
        List<CommentDto> comments = List.of(CommentDto.builder().id(1L).build());

        ItemDto result = ItemMapper.toItemDtoWithBookings(item, lastBooking, nextBooking, comments);

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getComments()).hasSize(1);
    }
}
