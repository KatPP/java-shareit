package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

/**
 * Контроллер для запросов на вещи в Gateway.
 * Обрабатывает все 4 эндпоинта из ТЗ.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Valid @RequestBody ItemRequestDto requestDto) {
        return itemRequestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long requestId) {
        return itemRequestClient.getById(userId, requestId);
    }
}