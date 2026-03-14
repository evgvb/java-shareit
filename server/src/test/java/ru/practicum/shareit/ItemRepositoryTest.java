package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest extends RepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager em;

    private User owner;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("Owner")
                .email("owner@email.com")
                .build();
        em.persist(owner);

        item1 = Item.builder()
                .name("Дрель")
                .description("Электрическая дрель")
                .available(true)
                .owner(owner)
                .build();

        item2 = Item.builder()
                .name("Отвертка")
                .description("Крестовая отвертка")
                .available(false)
                .owner(owner)
                .build();
    }

    @Test
    void findAllByOwnerId_shouldReturnItems() {
        em.persist(item1);
        em.persist(item2);

        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        assertThat(items).hasSize(2);
    }

    @Test
    void searchAvailableItems_whenTextMatchesName_shouldReturnItems() {
        em.persist(item1);

        List<Item> found = itemRepository.searchAvailableItems("Дрель");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void searchAvailableItems_whenTextMatchesDescription_shouldReturnItems() {
        em.persist(item1);

        List<Item> found = itemRepository.searchAvailableItems("электрическая");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDescription()).isEqualTo("Электрическая дрель");
    }

    @Test
    void searchAvailableItems_whenTextIsCaseInsensitive_shouldReturnItems() {
        em.persist(item1);

        List<Item> found = itemRepository.searchAvailableItems("ДРЕЛЬ");

        assertThat(found).hasSize(1);
    }

    @Test
    void searchAvailableItems_whenNoMatches_shouldReturnEmptyList() {
        em.persist(item1);

        List<Item> found = itemRepository.searchAvailableItems("несуществующий текст");

        assertThat(found).isEmpty();
    }

    @Test
    void searchAvailableItems_whenItemNotAvailable_shouldNotReturn() {
        em.persist(item2); // available = false

        List<Item> found = itemRepository.searchAvailableItems("Отвертка");

        assertThat(found).isEmpty();
    }

    @Test
    void saveItem_shouldSetId() {
        Item saved = itemRepository.save(item1);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Дрель");
        assertThat(saved.getOwner().getId()).isEqualTo(owner.getId());
    }
}