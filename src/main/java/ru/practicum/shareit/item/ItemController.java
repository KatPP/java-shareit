package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * REST-контроллер для управления вещами (items) в системе ShareIt.
 * Предоставляет эндпоинты для создания, обновления, поиска и получения информации о вещах.
 * Все операции, кроме получения по ID и поиска, требуют заголовок {@code X-Sharer-User-Id},
 * идентифицирующий владельца или автора запроса.
 */
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * Конструктор для внедрения зависимости сервиса вещей.
     *
     * @param itemService сервис для работы с вещами
     */
    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Создаёт новую вещь для указанного пользователя.
     *
     * @param userIdHeader значение заголовка {@code X-Sharer-User-Id} (обязательный)
     * @param itemDto      данные новой вещи (название, описание, статус доступности)
     * @return созданная вещь с присвоенным ID
     * @throws jakarta.validation.ValidationException если заголовок отсутствует, некорректен
     *                                                или данные вещи не прошли валидацию
     * @throws ru.practicum.shareit.exception.NotFoundException если пользователь не найден
     */
    @PostMapping
    public ItemDto create(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                          @Valid @RequestBody ItemDto itemDto) {
        Long userId = parseUserId(userIdHeader);
        log.info("Создание новой вещи пользователем ID={}: {}", userId, itemDto);
        ItemDto saved = itemService.create(userId, itemDto);
        log.info("Вещь создана: {}", saved);
        return saved;
    }

    /**
     * Частично обновляет данные существующей вещи по её идентификатору.
     * Обновление может включать любое подмножество полей: {@code name}, {@code description}, {@code available}.
     * Только владелец вещи может её обновлять.
     * @param userIdHeader значение заголовка {@code X-Sharer-User-Id} (обязательный)
     * @param itemId       идентификатор обновляемой вещи
     * @param itemDto      DTO с полями для обновления (остальные поля могут быть null)
     * @return обновлённая вещь
     * @throws jakarta.validation.ValidationException если заголовок отсутствует или некорректен
     * @throws ru.practicum.shareit.exception.NotFoundException если вещь не найдена или пользователь не является её владельцем
     */
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        Long userId = parseUserId(userIdHeader);
        log.info("Обновление вещи ID={} пользователем ID={}: {}", itemId, userId, itemDto);
        ItemDto updated = itemService.update(userId, itemId, itemDto);
        log.info("Вещь обновлена: {}", updated);
        return updated;
    }

    /**
     * Возвращает данные вещи по её идентификатору.
     * Заголовок {@code X-Sharer-User-Id} не требуется.
     *
     * @param itemId идентификатор вещи
     * @return данные вещи
     * @throws ru.practicum.shareit.exception.NotFoundException если вещь с указанным ID не найдена
     */
    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        log.info("Получение вещи по ID: {}", itemId);
        ItemDto item = itemService.getById(itemId);
        log.info("Вещь найдена: {}", item);
        return item;
    }

    /**
     * Возвращает список всех вещей, принадлежащих указанному пользователю.
     *
     * @param userIdHeader значение заголовка {@code X-Sharer-User-Id} (обязательный)
     * @return список вещей владельца
     * @throws jakarta.validation.ValidationException если заголовок отсутствует или некорректен
     * @throws ru.practicum.shareit.exception.NotFoundException если пользователь не найден
     */
    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        log.info("Получение всех вещей владельца ID={}", userId);
        List<ItemDto> items = itemService.getOwnerItems(userId);
        log.info("Найдено {} вещей", items.size());
        return items;
    }

    /**
     * Выполняет поиск вещей по текстовому запросу в названии или описании.
     * В результаты включаются только доступные для аренды вещи ({@code available = true}).
     * Регистр игнорируется. Поиск по пустой строке возвращает пустой список.
     * Заголовок {@code X-Sharer-User-Id} не требуется.
     *
     * @param text текст поискового запроса
     * @return список подходящих вещей
     */
    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(defaultValue = "") String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        List<ItemDto> results = itemService.search(text);
        log.info("Найдено {} результатов", results.size());
        return results;
    }

    /**
     * Парсит и валидирует значение заголовка {@code X-Sharer-User-Id}.
     *
     * @param userIdHeader строковое значение заголовка
     * @return идентификатор пользователя как {@code Long}
     * @throws jakarta.validation.ValidationException если заголовок отсутствует, пуст, не является числом
     *                                                или содержит недопустимое значение (≤ 0)
     */
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