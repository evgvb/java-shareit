package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void toItemRequestDto_ShouldConvertItemRequestToDto() {
        User requestor = User.builder().id(1L).name("Requester").build();
        LocalDateTime now = LocalDateTime.now();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(100L)
                .description("Need a tool")
                .requestor(requestor)
                .created(now)
                .build();

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getDescription()).isEqualTo("Need a tool");
        assertThat(dto.getRequestorId()).isEqualTo(1L);
        assertThat(dto.getRequestorName()).isEqualTo("Requester");
        assertThat(dto.getCreated()).isEqualTo(now);
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemRequestDto_ShouldReturnNull_WhenItemRequestIsNull() {
        assertThat(ItemRequestMapper.toItemRequestDto(null)).isNull();
    }

    @Test
    void toItemRequestDtoWithItems_ShouldIncludeItems() {
        User requestor = User.builder().id(1L).name("Requester").build();
        LocalDateTime now = LocalDateTime.now();

        ItemRequest itemRequest = ItemRequest.builder()
                .id(100L)
                .description("Need a tool")
                .requestor(requestor)
                .created(now)
                .build();

        ItemDto item1 = ItemDto.builder().id(10L).name("Tool1").build();
        ItemDto item2 = ItemDto.builder().id(11L).name("Tool2").build();
        List<ItemDto> items = List.of(item1, item2);

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDtoWithItems(itemRequest, items);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getItems().get(1).getId()).isEqualTo(11L);
    }

    @Test
    void toItemRequest_ShouldConvertCreateDtoToItemRequest() {
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Need a tool")
                .build();

        User requestor = User.builder().id(1L).name("Requester").build();

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(createDto, requestor);

        assertThat(itemRequest).isNotNull();
        assertThat(itemRequest.getDescription()).isEqualTo("Need a tool");
        assertThat(itemRequest.getRequestor()).isEqualTo(requestor);
        assertThat(itemRequest.getCreated()).isNotNull();
    }

    @Test
    void toItemRequest_ShouldReturnNull_WhenCreateDtoIsNull() {
        assertThat(ItemRequestMapper.toItemRequest(null, null)).isNull();
    }
}