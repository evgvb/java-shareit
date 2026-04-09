package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ItemRequestControllerTest extends BaseControllerTest {

    private ItemRequestDto itemRequestDto;
    private CreateItemRequestDto createItemRequestDto;
    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        createItemRequestDto = CreateItemRequestDto.builder()
                .description("нужна штука")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("нужна штука")
                .created(now)
                .requestorId(userId)
                .requestorName("user1")
                .items(List.of())
                .build();
    }

    @Test
    void createItemRequest_ShouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createItemRequest(any(CreateItemRequestDto.class), eq(userId)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createItemRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("нужна штука"))
                .andExpect(jsonPath("$.requestorId").value(userId))
                .andExpect(jsonPath("$.requestorName").value("user1"));

        verify(itemRequestService, times(1)).createItemRequest(any(CreateItemRequestDto.class), eq(userId));
    }

    @Test
    void createItemRequest_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(itemRequestService.createItemRequest(any(CreateItemRequestDto.class), eq(999L)))
                .thenThrow(new NoSuchElementException("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createItemRequestDto)))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).createItemRequest(any(CreateItemRequestDto.class), eq(999L));
    }

    @Test
    void getUserItemRequests_ShouldReturnListOfRequests() throws Exception {
        ItemRequestDto request2 = ItemRequestDto.builder()
                .id(2L)
                .description("нужна штука2")
                .created(now.minusDays(1))
                .requestorId(userId)
                .requestorName("user1")
                .build();

        List<ItemRequestDto> requests = Arrays.asList(itemRequestDto, request2);

        when(itemRequestService.getUserItemRequests(eq(userId), anyInt(), anyInt()))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].description").value("нужна штука"))
                .andExpect(jsonPath("$[1].description").value("нужна штука2"));

        verify(itemRequestService, times(1)).getUserItemRequests(eq(userId), eq(0), eq(10));
    }

    @Test
    void getUserItemRequests_WithPagination_ShouldPassParameters() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getUserItemRequests(eq(userId), eq(5), eq(20));
    }

    @Test
    void getAllItemRequests_ShouldReturnAllRequestsExceptUser() throws Exception {
        ItemRequestDto requestFromOther = ItemRequestDto.builder()
                .id(2L)
                .description("нужно чета")
                .created(now.minusDays(2))
                .requestorId(2L)
                .requestorName("user2")
                .build();

        List<ItemRequestDto> requests = List.of(requestFromOther);

        when(itemRequestService.getAllItemRequests(eq(userId), anyInt(), anyInt()))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].requestorId").value(2));

        verify(itemRequestService, times(1)).getAllItemRequests(eq(userId), eq(0), eq(10));
    }

    @Test
    void getAllItemRequests_WithPagination_ShouldPassParameters() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "10")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getAllItemRequests(eq(userId), eq(10), eq(5));
    }

    @Test
    void getItemRequestById_ShouldReturnRequest() throws Exception {
        when(itemRequestService.getItemRequestById(requestId, userId)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("нужна штука"))
                .andExpect(jsonPath("$.requestorId").value(userId));

        verify(itemRequestService, times(1)).getItemRequestById(requestId, userId);
    }

    @Test
    void getItemRequestById_ShouldReturn404_WhenRequestNotFound() throws Exception {
        when(itemRequestService.getItemRequestById(999L, userId))
                .thenThrow(new NoSuchElementException("Нет заявки"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequestById(999L, userId);
    }

    @Test
    void getItemRequestById_ShouldReturn404_WhenUserNotFound() throws Exception {
        when(itemRequestService.getItemRequestById(requestId, 999L))
                .thenThrow(new NoSuchElementException("Пользователь не найден"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequestById(requestId, 999L);
    }

    @Test
    void getItemRequestById_WithItems_ShouldIncludeItemsInResponse() throws Exception {
        // Создаем реальные ItemDto объекты вместо Map
        ItemDto item1 = ItemDto.builder()
                .id(1L)
                .name("штука1")
                .description("нужная штука1")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .id(2L)
                .name("штука2")
                .description("надо штуку2")
                .available(true)
                .build();

        ItemRequestDto requestWithItems = ItemRequestDto.builder()
                .id(requestId)
                .description("ннада штуку")
                .created(now)
                .requestorId(userId)
                .requestorName("user1")
                .items(Arrays.asList(item1, item2))
                .build();

        when(itemRequestService.getItemRequestById(requestId, userId)).thenReturn(requestWithItems);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("штука1"))
                .andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.items[1].name").value("штука2"));

        verify(itemRequestService, times(1)).getItemRequestById(requestId, userId);
    }
}
