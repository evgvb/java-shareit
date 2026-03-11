package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class ItemRequestTest extends IntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        UserDto user1 = UserDto.builder()
                .name("Пользователь 1")
                .email("user1@test.com")
                .build();
        userId1 = userService.createUser(user1).getId();

        UserDto user2 = UserDto.builder()
                .name("Пользователь 2")
                .email("user2@test.com")
                .build();
        userId2 = userService.createUser(user2).getId();
    }

    @Test
    void createItemRequest_ShouldCreateRequest_WhenDataIsValid() {
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна штука")
                .build();

        ItemRequestDto createdRequest = itemRequestService.createItemRequest(createDto, userId1);

        assertThat(createdRequest.getId()).isNotNull();
        assertThat(createdRequest.getDescription()).isEqualTo("Нужна штука");
        assertThat(createdRequest.getRequestorId()).isEqualTo(userId1);
        assertThat(createdRequest.getCreated()).isNotNull();
        assertThat(createdRequest.getItems()).isEmpty();
    }

    @Test
    void getUserItemRequests_ShouldReturnUserRequests() {
        CreateItemRequestDto createDto1 = CreateItemRequestDto.builder()
                .description("Нужна дрель")
                .build();
        CreateItemRequestDto createDto2 = CreateItemRequestDto.builder()
                .description("Нужен шуруповерт")
                .build();

        itemRequestService.createItemRequest(createDto1, userId1);
        itemRequestService.createItemRequest(createDto2, userId1);

        List<ItemRequestDto> userRequests = itemRequestService.getUserItemRequests(userId1, 0, 10);

        assertThat(userRequests).hasSize(2);
        assertThat(userRequests).extracting(ItemRequestDto::getDescription)
                .containsExactlyInAnyOrder("Нужен шуруповерт", "Нужна дрель"); // Порядок не важен
    }

    @Test
    void getAllItemRequests_ShouldReturnRequestsFromOtherUsers() {
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна штука")
                .build();

        itemRequestService.createItemRequest(createDto, userId1);

        List<ItemRequestDto> allRequests = itemRequestService.getAllItemRequests(userId2, 0, 10);

        assertThat(allRequests).hasSize(1);
        assertThat(allRequests.get(0).getDescription()).isEqualTo("Нужна штука");
        assertThat(allRequests.get(0).getRequestorId()).isEqualTo(userId1);
    }

    @Test
    void getItemRequestById_ShouldReturnRequestWithItems() {
        log.info("Тест: getItemRequestById_ShouldReturnRequestWithItems");

        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна штука")
                .build();

        ItemRequestDto createdRequest = itemRequestService.createItemRequest(createDto, userId1);
        log.info("Создан запрос: ID={}, description='{}'",
                createdRequest.getId(), createdRequest.getDescription());

        ItemDto itemDto = ItemDto.builder()
                .name("штука")
                .description("нужная штука")
                .available(true)
                .requestId(createdRequest.getId())
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, userId2);
        log.info("Создана вещь: ID={}, name='{}', requestId={}",
                createdItem.getId(), createdItem.getName(), createdItem.getRequestId());

        ItemRequestDto foundRequest = itemRequestService.getItemRequestById(createdRequest.getId(), userId2);

        log.info("Получен запрос: ID={}, description='{}'",
                foundRequest.getId(), foundRequest.getDescription());
        log.info("Количество вещей в запросе: {}", foundRequest.getItems().size());

        if (!foundRequest.getItems().isEmpty()) {
            ItemDto foundItem = foundRequest.getItems().get(0);
            log.info("Вещь: ID={}, name='{}', requestId={}",
                    foundItem.getId(), foundItem.getName(), foundItem.getRequestId());
        } else {
            log.warn("!!! Список вещей пуст!");
        }

        assertThat(foundRequest.getId()).isEqualTo(createdRequest.getId());
        log.info("ID запроса совпадает");

        assertThat(foundRequest.getDescription()).isEqualTo("Нужна штука");
        log.info("Описание запроса совпадает");

        assertThat(foundRequest.getItems()).hasSize(1);
        log.info("Количество вещей = 1");

        assertThat(foundRequest.getItems().get(0).getName()).isEqualTo("штука");
        log.info("Название вещи совпадает");

        assertThat(foundRequest.getItems().get(0).getRequestId()).isEqualTo(createdRequest.getId());
        log.info("requestId вещи совпадает с ID запроса");
    }

    @Test
    void createItemWithRequestId_ShouldLinkItemToRequest() {
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("Нужна штука")
                .build();
        ItemRequestDto createdRequest = itemRequestService.createItemRequest(createDto, userId1);

        ItemDto itemDto = ItemDto.builder()
                .name("штука")
                .description("штука")
                .available(true)
                .requestId(createdRequest.getId())
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, userId2);

        assertThat(createdItem.getRequestId()).isEqualTo(createdRequest.getId());

        ItemRequestDto foundRequest = itemRequestService.getItemRequestById(createdRequest.getId(), userId1);
        assertThat(foundRequest.getItems()).hasSize(1);
        assertThat(foundRequest.getItems().get(0).getId()).isEqualTo(createdItem.getId());
    }

    @Test
    void getItemRequestById_ShouldThrowException_WhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getItemRequestById(999L, userId1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }
}