package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemRequestMapper {
    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request, List<ItemDto> items) {
        if (request == null) return null;
        return new ItemRequestResponseDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }
}