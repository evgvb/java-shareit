package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание новой вещи пользователем с ID: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + ownerId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь создана с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        log.info("Обновление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        // Проверяем, что пользователь является владельцем - если нет, то 404
        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, itemDto);
        Item savedItem = itemRepository.update(updatedItem);

        log.info("Вещь с ID {} обновлена", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    public ItemDto partialUpdateItem(Long itemId, ItemUpdateDto updateDto, Long ownerId) {

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, updateDto);
        Item savedItem = itemRepository.update(updatedItem);

        log.info("Вещь с ID {} частично обновлена", itemId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи с ID: {} пользователем с ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца с ID: {}", ownerId);

        if (!userRepository.existsById(ownerId)) {
            throw new NoSuchElementException("Пользователь с ID " + ownerId + " не найден");
        }

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Long userId) {
        log.info("Поиск вещей по тексту: '{}' пользователем с ID: {}", text, userId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        log.info("Удаление вещи с ID: {} пользователем с ID: {}", itemId, ownerId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Вещь с ID " + itemId + " не найдена у пользователя с ID " + ownerId);
        }

        itemRepository.deleteById(itemId);
        log.info("Вещь с ID {} удалена", itemId);
    }
}