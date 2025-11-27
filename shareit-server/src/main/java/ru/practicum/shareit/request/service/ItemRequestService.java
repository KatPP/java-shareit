package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long requestorId, String description);

    List<ItemRequestDto> getOwn(Long requestorId);

    List<ItemRequestDto> getAll(Long requestorId, int from, int size);

    ItemRequestDto getById(Long requestId, Long userId);
}