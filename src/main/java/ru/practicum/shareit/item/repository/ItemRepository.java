package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> ownerItems = new HashMap<>();
    private Long idCounter = 1L;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idCounter++);
        }

        items.put(item.getId(), item);

        // Добавляем в индекс по владельцу
        Long ownerId = item.getOwner().getId();
        ownerItems.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(item);

        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        return ownerItems.getOrDefault(ownerId, Collections.emptyList());
    }

    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        List<Item> result = new ArrayList<>();

        for (Item item : items.values()) {
            if (Boolean.TRUE.equals(item.getAvailable()) &&
                    (item.getName().toLowerCase().contains(searchText) ||
                            item.getDescription().toLowerCase().contains(searchText))) {
                result.add(item);
            }
        }

        return result;
    }

    public Item update(Item item) {
        if (item.getId() == null || !items.containsKey(item.getId())) {
            throw new NoSuchElementException("Вещь не найдена");
        }

        Item oldItem = items.get(item.getId());

        item.setOwner(oldItem.getOwner());

        // Сохраняем оригинальный запрос, если он не указан в обновлении
        if (item.getRequest() == null) {
            item.setRequest(oldItem.getRequest());
        }

        items.put(item.getId(), item);
        return item;
    }

    public void deleteById(Long id) {
        Item item = items.remove(id);
        if (item != null) {
            // Удаляем из индекса по владельцу
            Long ownerId = item.getOwner().getId();
            List<Item> ownerItemsList = ownerItems.get(ownerId);
            if (ownerItemsList != null) {
                ownerItemsList.remove(item);
            }
        }
    }
}