package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * REST-контроллер для управления вещами.
 */
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Создание новой вещи пользователем ID={}: {}", userId, itemDto);
        ItemDto saved = itemService.create(userId, itemDto);
        log.info("Вещь создана: {}", saved);
        return saved;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Обновление вещи ID={} пользователем ID={}: {}", itemId, userId, itemDto);
        ItemDto updated = itemService.update(userId, itemId, itemDto);
        log.info("Вещь обновлена: {}", updated);
        return updated;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        log.info("Получение вещи по ID: {}", itemId);
        ItemDto item = itemService.getById(itemId);
        log.info("Вещь найдена: {}", item);
        return item;
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение всех вещей владельца ID={}", userId);
        List<ItemDto> items = itemService.getOwnerItems(userId);
        log.info("Найдено {} вещей", items.size());
        return items;
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(defaultValue = "") String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        List<ItemDto> results = itemService.search(text);
        log.info("Найдено {} результатов", results.size());
        return results;
    }
}