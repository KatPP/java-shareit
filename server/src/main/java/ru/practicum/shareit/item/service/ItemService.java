package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemResponseDto getByIdWithBookingsAndComments(Long itemId, Long userId);

    List<ItemResponseDto> getOwnerItemsWithBookingsAndComments(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);

    ru.practicum.shareit.item.model.Item getItemById(Long id);
}