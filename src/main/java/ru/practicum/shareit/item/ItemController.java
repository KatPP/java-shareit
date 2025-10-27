package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

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
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                          @Valid @RequestBody ItemDto itemDto) {
        Long userId = parseUserId(userIdHeader);
        log.info("Создание новой вещи пользователем ID={}: {}", userId, itemDto);
        ItemDto saved = itemService.create(userId, itemDto);
        log.info("Вещь создана: {}", saved);
        return saved;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) { // ← @Valid удалён
        Long userId = parseUserId(userIdHeader);
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
    public List<ItemDto> getOwnerItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
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

    private Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new jakarta.validation.ValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            if (userId <= 0) {
                throw new jakarta.validation.ValidationException("Некорректный идентификатор пользователя");
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new jakarta.validation.ValidationException("Некорректный идентификатор пользователя");
        }
    }
}