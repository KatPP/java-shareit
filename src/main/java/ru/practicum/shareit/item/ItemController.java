package ru.practicum.shareit.item;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private Long parseUserIdHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ValidationException("Заголовок X-Sharer-User-Id обязателен");
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            if (userId <= 0) {
                throw new ValidationException("Некорректный идентификатор пользователя");
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new ValidationException("Некорректный идентификатор пользователя");
        }
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                          @RequestBody ItemDto itemDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userIdHeader,
                                   @PathVariable Long itemId) {
        Long userId = (userIdHeader != null && !userIdHeader.isBlank()) ? parseUserIdHeader(userIdHeader) : null;
        return itemService.getByIdWithBookingsAndComments(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") String userIdHeader) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemService.getOwnerItemsWithBookingsAndComments(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(defaultValue = "") String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") String userIdHeader,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        Long userId = parseUserIdHeader(userIdHeader);
        return itemService.addComment(userId, itemId, commentDto);
    }
}