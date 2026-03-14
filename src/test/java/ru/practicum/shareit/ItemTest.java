package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ItemTest extends IntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = UserDto.builder()
                .name("Владелец Вещей")
                .email("owner@test.com")
                .build();

        UserDto savedOwner = userService.createUser(owner);
        ownerId = savedOwner.getId();
    }

    @Test
    void createItem_ShouldSaveItem_WhenDataIsValid() {

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, ownerId);

        assertThat(createdItem.getId()).isNotNull();
        assertThat(createdItem.getName()).isEqualTo("Дрель");
        assertThat(createdItem.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(createdItem.getAvailable()).isTrue();
        assertThat(createdItem.getOwnerId()).isEqualTo(ownerId);

        assertThat(itemRepository.findById(createdItem.getId())).isPresent();
    }

    @Test
    void createItem_ShouldThrowException_WhenOwnerDoesNotExist() {

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.createItem(itemDto, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getItemById_ShouldReturnItem_WhenItemExists() {

        ItemDto itemDto = ItemDto.builder()
                .name("Перфоратор")
                .description("Мощный перфоратор")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        ItemDto foundItem = itemService.getItemById(savedItem.getId(), ownerId);

        assertThat(foundItem.getId()).isEqualTo(savedItem.getId());
        assertThat(foundItem.getName()).isEqualTo("Перфоратор");
        assertThat(foundItem.getDescription()).isEqualTo("Мощный перфоратор");
        assertThat(foundItem.getAvailable()).isTrue();
    }

    @Test
    void getItemById_ShouldThrowException_WhenItemDoesNotExist() {
        assertThatThrownBy(() -> itemService.getItemById(999L, ownerId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void updateItem_ShouldUpdateName_WhenOnlyNameProvided() {

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Обычная дрель")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        Map<String, Object> updates = Map.of("name", "Дрель Professional");
        ItemDto updatedItem = itemService.updateItem(savedItem.getId(), updates, ownerId);

        assertThat(updatedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(updatedItem.getName()).isEqualTo("Дрель Professional");
        assertThat(updatedItem.getDescription()).isEqualTo("Обычная дрель");
        assertThat(updatedItem.getAvailable()).isTrue();
    }

    @Test
    void updateItem_ShouldUpdateAvailable_WhenAvailableProvided() {

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Обычная дрель")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        Map<String, Object> updates = Map.of("available", false);
        ItemDto updatedItem = itemService.updateItem(savedItem.getId(), updates, ownerId);

        assertThat(updatedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(updatedItem.getName()).isEqualTo("Дрель");
        assertThat(updatedItem.getDescription()).isEqualTo("Обычная дрель");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    void updateItem_ShouldThrowException_WhenUserIsNotOwner() {

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Обычная дрель")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        UserDto otherUser = UserDto.builder()
                .name("Другой пользователь")
                .email("other@test.com")
                .build();

        UserDto savedOtherUser = userService.createUser(otherUser);

        Map<String, Object> updates = Map.of("name", "Попытка взлома");

        assertThatThrownBy(() -> itemService.updateItem(savedItem.getId(), updates, savedOtherUser.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найдена у пользователя");
    }

    @Test
    void getAllItemsByOwner_ShouldReturnAllOwnerItems() {

        ItemDto item1 = ItemDto.builder().name("Вещь 1").description("Описание 1").available(true).build();
        ItemDto item2 = ItemDto.builder().name("Вещь 2").description("Описание 2").available(true).build();
        ItemDto item3 = ItemDto.builder().name("Вещь 3").description("Описание 3").available(true).build();

        itemService.createItem(item1, ownerId);
        itemService.createItem(item2, ownerId);
        itemService.createItem(item3, ownerId);

        List<ItemDto> ownerItems = itemService.getAllItemsByOwner(ownerId, 0, 10);

        // Проверка
        assertThat(ownerItems).hasSize(3);
        assertThat(ownerItems).extracting(ItemDto::getName)
                .containsExactly("Вещь 1", "Вещь 2", "Вещь 3");
    }

    @Test
    void getAllItemsByOwner_ShouldReturnEmptyList_WhenOwnerHasNoItems() {

        List<ItemDto> ownerItems = itemService.getAllItemsByOwner(ownerId, 0, 10);

        assertThat(ownerItems).isEmpty();
    }

    @Test
    void searchItems_ShouldReturnAvailableItemsMatchingText() {

        ItemDto item1 = ItemDto.builder()
                .name("Дрель Makita")
                .description("Японская дрель")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .name("Шуруповерт Bosch")
                .description("Немецкий шуруповерт")
                .available(true)
                .build();

        ItemDto item3 = ItemDto.builder()
                .name("Молоток")
                .description("Обычный молоток")
                .available(false)  // Недоступна
                .build();

        itemService.createItem(item1, ownerId);
        itemService.createItem(item2, ownerId);
        itemService.createItem(item3, ownerId);

        List<ItemDto> searchResults = itemService.searchItems("дрель", ownerId, 0, 10);

        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getName()).isEqualTo("Дрель Makita");

        // Поиск по слову "шуруповерт"
        searchResults = itemService.searchItems("шуруповерт", ownerId, 0, 10);
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getName()).isEqualTo("Шуруповерт Bosch");

        // Поиск по несуществующему тексту
        searchResults = itemService.searchItems("xyz123", ownerId, 0, 10);
        assertThat(searchResults).isEmpty();
    }

    @Test
    void deleteItem_ShouldRemoveItem_WhenUserIsOwner() {

        ItemDto itemDto = ItemDto.builder()
                .name("Для удаления")
                .description("Будет удалена")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        itemService.deleteItem(savedItem.getId(), ownerId);

        assertThat(itemRepository.findById(savedItem.getId())).isEmpty();
        assertThatThrownBy(() -> itemService.getItemById(savedItem.getId(), ownerId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteItem_ShouldThrowException_WhenUserIsNotOwner() {

        ItemDto itemDto = ItemDto.builder()
                .name("Чужая вещь")
                .description("Не моя вещь")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, ownerId);

        UserDto otherUser = UserDto.builder()
                .name("Другой пользователь")
                .email("other@test.com")
                .build();

        UserDto savedOtherUser = userService.createUser(otherUser);

        assertThatThrownBy(() -> itemService.deleteItem(savedItem.getId(), savedOtherUser.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найдена у пользователя");
    }
}
