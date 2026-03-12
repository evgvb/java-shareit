package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemControllerTest extends BaseControllerTest {

    private ItemDto itemDto;
    private CommentDto commentDto;
    private final Long userId = 1L;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(itemId)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(userId)
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Great tool!")
                .authorName("John Doe")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(userId))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Powerful drill"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService, times(1)).createItem(any(ItemDto.class), eq(userId));
    }

    @Test
    void createItem_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(999L)))
                .thenThrow(new NoSuchElementException("User not found"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).createItem(any(ItemDto.class), eq(999L));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        Map<String, Object> updates = Map.of("name", "Updated Drill");
        ItemDto updatedDto = ItemDto.builder()
                .id(itemId)
                .name("Updated Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(userId)
                .build();

        when(itemService.updateItem(eq(itemId), any(Map.class), eq(userId))).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));

        verify(itemService, times(1)).updateItem(eq(itemId), any(Map.class), eq(userId));
    }

    @Test
    void updateItem_ShouldReturn404_WhenItemNotFound() throws Exception {
        when(itemService.updateItem(eq(999L), any(Map.class), eq(userId)))
                .thenThrow(new NoSuchElementException("Item not found"));

        mockMvc.perform(patch("/items/999")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "test"))))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).updateItem(eq(999L), any(Map.class), eq(userId));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        when(itemService.getItemById(itemId, userId)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService, times(1)).getItemById(itemId, userId);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnListOfItems() throws Exception {
        List<ItemDto> items = Arrays.asList(itemDto,
                ItemDto.builder().id(2L).name("Hammer").description("Heavy hammer").available(true).build());

        when(itemService.getAllItemsByOwner(eq(userId), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService, times(1)).getAllItemsByOwner(eq(userId), eq(0), eq(10));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.searchItems(eq("drill"), eq(userId), anyInt(), anyInt())).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemService, times(1)).searchItems(eq("drill"), eq(userId), eq(0), eq(10));
    }

    @Test
    void searchItems_ShouldReturnEmptyList_WhenTextIsEmpty() throws Exception {
        // Убираем verify, так как метод не должен вызываться при пустом тексте
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemService, never()).searchItems(anyString(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void deleteItem_ShouldReturnNoContent() throws Exception {
        doNothing().when(itemService).deleteItem(itemId, userId);

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).deleteItem(itemId, userId);
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        CreateCommentDto createCommentDto = CreateCommentDto.builder()
                .text("Great tool!")
                .build();

        when(itemService.addComment(eq(itemId), any(CreateCommentDto.class), eq(userId)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great tool!"))
                .andExpect(jsonPath("$.authorName").value("John Doe"));

        verify(itemService, times(1)).addComment(eq(itemId), any(CreateCommentDto.class), eq(userId));
    }
}