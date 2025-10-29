package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Сервис для управления вещами с in-memory хранилищем.
 */
@Service
@Slf4j
public class ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final UserService userService;

    @Autowired
    public ItemService(UserService userService) {
        this.userService = userService;
    }

    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем ID={}: {}", userId, itemDto);
        validateUserId(userId);
        validateItemDto(itemDto);
        Item item = ItemMapper.toItem(itemDto);
        item.setId(idGenerator.getAndIncrement());
        item.setOwner(userService.getUserById(userId));
        items.put(item.getId(), item);
        log.info("Вещь создана с ID={}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи ID={} пользователем ID={}: {}", itemId, userId, itemDto);
        validateUserId(userId);
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Попытка обновить чужую вещь ID={}, владелец={}", itemId, item.getOwner().getId());
            throw new NotFoundException("Вещь не найдена");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        log.info("Вещь обновлена: {}", item);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto getById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            log.warn("Вещь с ID={} не найдена", itemId);
            throw new NotFoundException("Вещь с ID " + itemId + " не найдена");
        }
        log.info("Вещь найдена: {}", item);
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getOwnerItems(Long userId) {
        log.info("Получение всех вещей владельца ID={}", userId);
        userService.getUserById(userId);
        List<ItemDto> ownerItems = items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Найдено {} вещей", ownerItems.size());
        return ownerItems;
    }

    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.info("Поиск по пустому тексту — возвращаем пустой список");
            return Collections.emptyList();
        }
        String query = text.toLowerCase();
        List<ItemDto> results = items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query) ||
                        item.getDescription().toLowerCase().contains(query))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Поиск по тексту '{}': найдено {} результатов", text, results.size());
        return results;
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.error("Некорректный идентификатор пользователя: {}", userId);
            throw new ValidationException("Некорректный идентификатор пользователя");
        }
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto == null) {
            log.error("Данные вещи не могут быть null");
            throw new ValidationException("Данные вещи не могут быть null");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.error("Название вещи не может быть пустым");
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.error("Описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            log.error("Статус доступности (available) обязателен");
            throw new ValidationException("Статус доступности (available) обязателен");
        }
    }

    private Item getItemOrThrow(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            log.warn("Вещь с ID={} не найдена", itemId);
            throw new NotFoundException("Вещь с ID " + itemId + " не найдена");
        }
        return item;
    }
}