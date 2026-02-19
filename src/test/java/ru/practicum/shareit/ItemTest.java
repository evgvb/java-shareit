package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {
    private ItemRepository itemRepository;
    private User owner;

    @BeforeEach
    void setUp() {
        itemRepository = new ItemRepository();

        // владелец для тестовых вещей
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@email.com")
                .build();
    }

    // сохранение вещи
    @Test
    void save_shouldSaveNewItem() {
        Item item = Item.builder()
                .name("Item 1")
                .description("item 1 описание")
                .available(true)
                .owner(owner)
                .build();

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem.getId(), "Сохраненной вещи должен быть присвоен ID");
        assertEquals("Item 1", savedItem.getName(), "Имя должно сохраниться");
        assertTrue(savedItem.getAvailable(), "Статус доступности должен сохраниться");
        assertEquals(owner.getId(), savedItem.getOwner().getId(), "ID владельца должен сохраниться");
    }

    // поиск вещей по тексту
    @Test
    void search_shouldReturnAvailableItemsContainingText() {
        Item item1 = Item.builder()
                .name("Item 1")
                .description("item 1 описание")
                .available(true)  // Доступна
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .name("Thing")
                .description("thing описание")
                .available(true)  // Доступна
                .owner(owner)
                .build();

        Item item3 = Item.builder()
                .name("Item 3")
                .description("item 3 описание")
                .available(false)  // Недоступна
                .owner(owner)
                .build();

        // Сохраняем вещи
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        List<Item> result = itemRepository.search("item");

        assertEquals(1, result.size(), "Должна вернуться только одна доступная вещь с 'item1'");
        assertEquals("Item 1", result.get(0).getName(), "Должна вернуться вещь 'Item 1'");
        assertTrue(result.get(0).getAvailable(), "Вернувшаяся вещь должна быть доступна");
    }

    // поиск вещей владельца
    @Test
    void findAllByOwnerId_shouldReturnOwnerItems() {
        Item item1 = Item.builder()
                .name("Item 1")
                .description("item 1 описание")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .name("Item 2")
                .description("item 2 описание")
                .available(true)
                .owner(owner)
                .build();

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        assertEquals(2, items.size(), "Должны вернуться 2 вещи владельца");
    }
}

