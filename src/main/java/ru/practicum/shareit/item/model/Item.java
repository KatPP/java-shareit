package ru.practicum.shareit.item.model;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.request.ItemRequest;
import lombok.*;

/**
 * Модель вещи, доступной для аренды.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;
}