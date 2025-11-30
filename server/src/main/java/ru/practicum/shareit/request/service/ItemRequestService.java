package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(Long userId, String description);
    List<ItemRequestResponseDto> getOwnRequests(Long userId);
    List<ItemRequestResponseDto> getAllOtherRequests(Long userId, int from, int size);
    ItemRequestResponseDto getRequestById(Long requestId);
}