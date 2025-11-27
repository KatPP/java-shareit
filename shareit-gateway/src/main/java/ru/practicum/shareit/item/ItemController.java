package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.exception.HeaderValidationException;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new HeaderValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long id = Long.parseLong(userIdHeader);
            if (id <= 0) {
                throw new HeaderValidationException("Некорректный идентификатор пользователя");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new HeaderValidationException("Некорректный идентификатор пользователя");
        }
    }

    @PostMapping
    public Object create(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                         @Valid @RequestBody Object itemDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemClient.create(userId, itemDto).getBody();
    }

    @PatchMapping("/{itemId}")
    public Object update(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                         @PathVariable Long itemId,
                         @RequestBody Object itemDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemClient.update(userId, itemId, itemDto).getBody();
    }

    @GetMapping("/{itemId}")
    public Object getItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                          @PathVariable Long itemId) {
        Long userId = null;
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            userId = parseUserIdHeader(userIdHeader);
        }
        return itemClient.getById(userId, itemId).getBody();
    }

    @GetMapping
    public Object getOwnerItems(@RequestHeader("X-Sharer-User-Id") String userIdHeader) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemClient.getOwnerItems(userId).getBody();
    }

    @GetMapping("/search")
    public Object search(@RequestParam(defaultValue = "") String text) {
        return itemClient.search(text).getBody();
    }

    @PostMapping("/{itemId}/comment")
    public Object addComment(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                             @PathVariable Long itemId,
                             @RequestBody Object commentDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemClient.addComment(userId, itemId, commentDto).getBody();
    }
}