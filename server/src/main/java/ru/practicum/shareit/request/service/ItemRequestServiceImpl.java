package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, String description) {
        userService.getUserById(userId);
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(userService.getUserById(userId));
        request.setCreated(LocalDateTime.now());
        ItemRequest saved = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestResponseDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllOtherRequests(Long userId, int from, int size) {
        userService.getUserById(userId);
        List<ItemRequest> ownRequests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        List<Long> ownIds = ownRequests.stream().map(ItemRequest::getId).toList();

        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> others = ownIds.isEmpty()
                ? itemRequestRepository.findAll(page).getContent()
                : itemRequestRepository.findByIdNotInOrderByCreatedDesc(ownIds, page);

        return others.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));
        return mapToResponseDto(request);
    }

    private ItemRequestResponseDto mapToResponseDto(ItemRequest request) {
        List<ItemDto> items = itemRepository.findByRequest(request.getId()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return ItemRequestMapper.toItemRequestResponseDto(request, items);
    }
}